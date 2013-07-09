/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.btcashier.listener;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.azazar.bitcoin.jsonrpcclient.Bitcoin.Block;
import com.azazar.bitcoin.jsonrpcclient.BitcoinAcceptor;
import com.azazar.bitcoin.jsonrpcclient.BitcoinException;
import com.azazar.bitcoin.jsonrpcclient.SimpleBitcoinPaymentListener;
import com.btcashier.dao.AddressDao;
import com.btcashier.dao.DepositDao;
import com.btcashier.dao.ProcessedBlockDao;
import com.btcashier.domain.Address;
import com.btcashier.domain.Deposit;
import com.btcashier.domain.ProcessedBlock;
import com.btcashier.jms.QueueHelper;
import com.btcashier.utils.ExtendedBitcoinJSONRPCClient;

@Component
public class NewBlocksListener {

    private final static Logger log = Logger.getLogger(NewBlocksListener.class);

    @Autowired
    private ExtendedBitcoinJSONRPCClient client;

    private BitcoinAcceptor acceptor;

    @Autowired
    private QueueHelper queueHelper;

    @Autowired
    private AddressDao addressDao;

    @Autowired
    private DepositDao depositDao;

    @Autowired
    private ProcessedBlockDao processedBlockDao;

    @Autowired
    @Qualifier("newBlocksMonitorTaskExecutor")
    private TaskExecutor taskExecutor;

    @PostConstruct
    public void init() throws MalformedURLException, BitcoinException {
        taskExecutor.execute(new Runnable() {

            @Override
            public void run() {
                startup();
            }
        });
    }

    private void startup() {
        log.info("NewBlocksListener.init: " + Arrays.asList(Thread.currentThread().getStackTrace()));
        initAcceptor();
    }

    private void initAcceptor() {
        acceptor = new BitcoinAcceptor(client, new SimpleBitcoinPaymentListener() {

            @Override
            public void block(String blockHash) {
                // block discovered, handle it:
                processBlock(blockHash, true);
            }
        });
        taskExecutor.execute(acceptor);
    }

    @PreDestroy
    public void destroy() {
        if (null != acceptor) {
            acceptor.stopAccepting();
        }
    }

    /*package scope*/void processBlock(String blockHash, boolean tryProcessMissingBlocks) {
        try {
            final Block block = client.getBlock(blockHash);

            if (isAlreadyProcessed(block)) {
                return;
            }

            final List<Block> blocksToProcess = checkForBlockChainFork(block);

            if (null != blocksToProcess && blocksToProcess.size() > 0) {
                for (Block b : blocksToProcess) {
                    final List<String> txHashes = b.tx();
                    log.info("Discovered block to process: " + b.hash() + ", with block height : " + b.height() + ", with transactions number: " + txHashes.size());
                    final int blockHeight = b.height();
                    processTxs(txHashes, b);
                    recalculateExistingDepositsHeight(blockHeight);
                }
            } else {
                final List<String> txHashes = block.tx();
                log.info("Discovered block to process: " + blockHash + ", with block height : " + block.height() + ", with transactions number: " + txHashes.size());
                final int blockHeight = block.height();
                processTxs(txHashes, block);
                recalculateExistingDepositsHeight(blockHeight);
            }
        } catch (BitcoinException e) {
            log.error("Exception while processing newly discovered block", e);
            // TODO: consider if it not better to shutdown this listener in that case and mail admin
        } catch (DoNotProcessBlockMarkerException dnpbme) {
            log.warn("Skip, block already processed: " + blockHash, dnpbme);
        }
    }

    private boolean isAlreadyProcessed(Block block) {
        final ProcessedBlock pb = processedBlockDao.getByHash(block.hash());
        return null != pb;
    }

    private static class DoNotProcessBlockMarkerException extends RuntimeException {

        public DoNotProcessBlockMarkerException(String msg) {
            super(msg);
        }

        private static final long serialVersionUID = 1645675756753654756L;

    }

    private List<Block> checkForBlockChainFork(Block block) throws BitcoinException {
        final ProcessedBlock highestKnownBlock = processedBlockDao.getLastProcessedBlock();
        if (null == highestKnownBlock) {
            // 1st known block, so it definitely is not a fork
            final ProcessedBlock pb = createProcessedBlock(block, null);
            return null;
        }

        // check if the new PB if added will be the highest block. If not abandon it.
        if (block.height() <= highestKnownBlock.getHeight()) {
            throw new DoNotProcessBlockMarkerException("Discovered block: " + block.hash() + " has height <= from the existing highest known block: " + highestKnownBlock.getHash() + " (possible start of fork, but we are not sure yet)");
        }

        // check if new block can be added directly to the end of the known blockchain
        if (highestKnownBlock.getHash().equals(block.previousHash())) {
            final ProcessedBlock pb = createProcessedBlock(block, highestKnownBlock);
            return null;
        }

        // find appropriate place in the existing block chain (known to us), which is not the end of the known blockchain (this case was already handled above)
        Block blockToAnalyze = block;
        String prevHash = blockToAnalyze.previousHash();

        final List<Block> blocksToAdd = new ArrayList<>();
        int iteration = 0;
        do {
            iteration++;
            final ProcessedBlock predecessor = processedBlockDao.getByHash(prevHash);
            if (null != predecessor) {
                log.info("Found predecessor: " + predecessor.getHash() + " for " + blockToAnalyze.hash() + " in iteration: " + iteration);

                // abandon forked ProcessedBlock's and their transactions:
                abandonForkedTransactionsFrom(predecessor);

                ProcessedBlock newPredecessor = createProcessedBlock(blockToAnalyze, predecessor);
                for (int i = blocksToAdd.size() - 2 /*last block is the blockToAnalyze in fact*/; i >= 0; i--) {
                    final Block b = blocksToAdd.get(i);
                    newPredecessor = createProcessedBlock(b, newPredecessor);
                }

                return blocksToAdd;
            } else {
                blocksToAdd.add(blockToAnalyze);
                log.info("Don't found predecessor for " + blockToAnalyze.hash() + " in iteration: " + iteration + ", blocksToAdd.size: " + blocksToAdd.size());

                blockToAnalyze = client.getBlock(prevHash);
                if (null == blockToAnalyze) {
                    final String msg = "Can not find block with hash: " + prevHash;
                    log.fatal(msg);
                    throw new IllegalStateException(msg);
                }
                prevHash = blockToAnalyze.previousHash();
                log.info("New block to analyze: " + blockToAnalyze.hash() + ", prevHash: " + prevHash);
            }
        } while (true);
    }

    private void abandonForkedTransactionsFrom(ProcessedBlock startingBlock) {
        // find any further ProcessedBlock AFTER given startingBlock:
        ProcessedBlock block = startingBlock;
        log.info("Abandoning deposits starting from block: " + startingBlock.getHash());
        while (null != (block = processedBlockDao.getNextProcessedBlock(block))) {
            block.setFork(true);
            processedBlockDao.saveOrUpdate(block);
            for (Deposit dep : block.getDeposits()) {
                log.info("Abandoning deposit: " + dep.getId());
                queueHelper.queueDepositsChangeToProcess(dep);
            }
        }
    }

    private ProcessedBlock createProcessedBlock(Block block, ProcessedBlock previousPB) {
        final ProcessedBlock pb = new ProcessedBlock();
        pb.setFork(false);
        pb.setHash(block.hash());
        pb.setHeight(block.height());
        pb.setPrevious(previousPB);
        processedBlockDao.create(pb);
        return pb;
    }

    private void recalculateExistingDepositsHeight(int blockHeight) {
        final int updatedRows = depositDao.increaseNumberOfConfirmations(blockHeight);
        log.info("Number of updated rows/deposits: " + updatedRows + " for new block height: " + blockHeight);
        if (updatedRows > 0) {
            log.info("Started queueing deposit processing");
            for (Deposit dep : depositDao.getPerSaleUniqueAndWithRequiredOrLessConfirmations()) {
                log.info("Updated deposit queued to process: " + dep);
                queueHelper.queueDepositsChangeToProcess(dep);
            }
        }
    }

    private void processTxs(List<String> txHashes, Block block) {
        //int i = 0;
        final Set<String> failedTxs = new HashSet<String>();

        for (String txHash : txHashes) {
            try {
                //i++;
                final Map tx = client.getTx(txHash);
                //                log.info("tx idx " + i + ": " + txHash);
                //                log.info("tx ALL: " + tx);
                findAddressedMatch(tx, block);
            } catch (BitcoinException e) {
                log.error("Exception while processing obtaining transaction details for tx: " + txHash, e);
                failedTxs.add(txHash);
            }
        }
        if (failedTxs.size() > 0) {
            log.warn("Number of failed transactions: " + failedTxs.size() + " out of: " + txHashes.size());
        }

    }

    private static class AddressMapping {

        private Long n;
        private Double value;

    }

    private void findAddressedMatch(Map tx, Block block) {
        final List<Map> vouts = (List) tx.get("vout");
        final Long time = (Long) tx.get("time");
        //        System.out.println("vouts: " + vouts);
        final String txid = (String) tx.get("txid");
        log.info("Examining transaction: " + txid + " seen in block: " + block.hash());
        final Long numberConfirmations = (Long) tx.get("confirmations");

        final Map<String, AddressMapping> addressMapping = new HashMap<>();
        for (Map vout : vouts) {
            final Map scriptPubKey = (Map) vout.get("scriptPubKey");
            final List<String> addresses = (List<String>) scriptPubKey.get("addresses");
            if (addresses != null && addresses.size() == 1) {
                final AddressMapping am = new AddressMapping();
                final Long n = (Long) vout.get("n");
                final Double value = (Double) vout.get("value");
                final String btcAddress = addresses.get(0);
                am.n = n;
                am.value = value;
                addressMapping.put(btcAddress, am);
            } else {
                log.warn("We do not support vouts with other than one address: " + addresses + ", txid: " + txid);
            }
        }

        if (addressMapping.size() > 0) {
            final List<Address> addresses = addressDao.getByBtcAddresses(addressMapping.keySet());
            final ProcessedBlock pb = processedBlockDao.getByHash(block.hash());
            for (Address address : addresses) {
                final String btcAddress = address.getBtcAddress();
                final AddressMapping am = addressMapping.get(btcAddress);
                log.info("btcAddress found to notify: " + btcAddress);
                final String transactionIdAndNAndBlockTime = txid + ":" + am.n + ":" + block.time().getTime();
                verifyDepositUniqunessAndCreate(time, numberConfirmations, address, am, transactionIdAndNAndBlockTime, pb);
            }
        } else {
            log.warn("No addresses gathered for txid: " + txid);
        }

    }

    @Transactional(readOnly = false)
    private void verifyDepositUniqunessAndCreate(final Long time, final Long numberConfirmations, Address address, final AddressMapping am, final String transactionIdAndNAndBlockTime, ProcessedBlock pb) {
        if (null == depositDao.getNotForkedByTransactionIdAndNAndBlockTime(transactionIdAndNAndBlockTime)) {
            final BigInteger amount = convertToLongAmount(am.value);
            log.info("value: " + am.value + "; amount: " + amount + ", transactionIdAndNAndBlockTime: " + transactionIdAndNAndBlockTime);
            final Deposit dep = new Deposit();
            dep.setAmount(amount);
            dep.setAddress(address);
            dep.setTransactionIdAndNAndBlockTime(transactionIdAndNAndBlockTime);
            dep.setTime(time);
            dep.setBlock(pb);
            dep.setNumberConfirmations(numberConfirmations);
            depositDao.create(dep);
            depositDao.flush(dep);
            queueHelper.queueDepositsChangeToProcess(dep);
        } else {
            log.warn("We already processed transaction for transactionIdAndNAndBlockTime: " + transactionIdAndNAndBlockTime);
        }
    }

    /*package scope*/static BigInteger convertToLongAmount(final Double value) {
        return BigDecimal.valueOf(value).multiply(Deposit.BTC_MULTIPLIER_BD).setScale(0).toBigIntegerExact();
    }

    /*package scope*/void setClient(ExtendedBitcoinJSONRPCClient client) {
        this.client = client;
    }

    /*package scope*/void setAddressDao(AddressDao addressDao) {
        this.addressDao = addressDao;
    }

    /*package scope*/void setDepositDao(DepositDao depositDao) {
        this.depositDao = depositDao;
    }

    /*package scope*/void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

}
