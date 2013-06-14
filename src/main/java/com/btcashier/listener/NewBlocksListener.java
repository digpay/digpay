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
import com.azazar.bitcoin.jsonrpcclient.BitcoinJSONRPCClient;
import com.azazar.bitcoin.jsonrpcclient.SimpleBitcoinPaymentListener;
import com.btcashier.dao.AddressDao;
import com.btcashier.dao.DepositDao;
import com.btcashier.dao.KeyValueDao;
import com.btcashier.domain.Address;
import com.btcashier.domain.Deposit;
import com.btcashier.domain.KeyValue;
import com.btcashier.domain.enums.Keys;
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
    private KeyValueDao keyValueDao;

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

        // init DB with the not yet processed blocks
        try {
            initNotProcessedBlocks();
        } catch (BitcoinException e) {
            log.fatal("Cannot properly initialize processed blocks", e);
            throw new IllegalStateException(e);
        }
        initAcceptor();
    }

    private void initNotProcessedBlocks() throws BitcoinException {
        KeyValue processedBlocks = getProcessedBlockKV();
        int startProcessingFromBlockIdx = 0;
        if (null != processedBlocks) {
            final int lastProcessedBlockIdx = processedBlocks.getIntValue();
            startProcessingFromBlockIdx = lastProcessedBlockIdx + 1;
        } else {
            processedBlocks = new KeyValue(Keys.PROCESSED_BLOCKS);
            processedBlocks.setValue("0");
            processedBlocks = keyValueDao.saveOrUpdate(processedBlocks);
        }

        log.info("Currently the highest block idx: " + client.getBlockCount());
        while (startProcessingFromBlockIdx <= client.getBlockCount()) {
            final String blockHash = client.getBlockHash(startProcessingFromBlockIdx);
            log.info("Started processing block: " + startProcessingFromBlockIdx + ", with hash: " + blockHash);
            processBlock(blockHash, false);
            increaseProcessedBlockKV(startProcessingFromBlockIdx);
            startProcessingFromBlockIdx++;
        }
    }

    private KeyValue getProcessedBlockKV() {
        KeyValue processedBlocks = keyValueDao.getByKey(Keys.PROCESSED_BLOCKS);
        return processedBlocks;
    }
    
    private int getProcessedBlockIdx() {
        KeyValue processedBlocks = getProcessedBlockKV();
        int lastProcessedBlockIdx = processedBlocks.getIntValue();
        return lastProcessedBlockIdx;
    }

    private void initAcceptor() {
        acceptor = new BitcoinAcceptor(client, new SimpleBitcoinPaymentListener() {
            @Override
            public void block(String blockHash) {
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

            // if there are not processed blocks between current highest block and our last processed block, process the missing blocks:
            if (tryProcessMissingBlocks) {
                processMissingBlocksUpToGiven(block);
            }

            final List<String> txHashes = block.tx();
            log.info("Discovered block to process: " + blockHash + ", with blockId : " + block + ", with transactions number: " + txHashes.size());
            final int blockHeight = block.height();
            processTxs(txHashes, blockHeight);
            recalculateExistingDepositsHeight(blockHeight);
            increaseProcessedBlockKV(blockHeight);
        } catch (BitcoinException e) {
            log.error("Exception while processing newly discovered block", e);
            // TODO: consider if it not better to shutdown this listener in that case and mail admin
        }
    }

    private void recalculateExistingDepositsHeight(int blockHeight) {
        final int updatedRows = depositDao.increaseNumberOfConfirmations(blockHeight);
        log.info("Number of updated rows/deposits: " + updatedRows + " for new block height: " + blockHeight);
        if (updatedRows > 0) {
            log.info("Started queueing deposit processing");
            for (Deposit dep : depositDao.getPerSaleUniqueAndWithRequiredOrLessConfirmations()) {
                log.info("Updated deposit queued to process: " + dep);
                queueHelper.queueNewDepositToProcess(dep);
            }
        }
    }

    private void processMissingBlocksUpToGiven(final Block block) throws BitcoinException {
        final int lastProcessedBlockIdx = getProcessedBlockIdx();
        if (block.height() > 1 + lastProcessedBlockIdx) {
            int startProcessingFromBlockIdx = lastProcessedBlockIdx + 1;
            while (startProcessingFromBlockIdx < block.height()) {
                final String blockHashInner = client.getBlockHash(startProcessingFromBlockIdx);
                log.info("Started processing block: " + startProcessingFromBlockIdx + ", with hash: " + blockHashInner);
                processBlock(blockHashInner, false);
                increaseProcessedBlockKV(startProcessingFromBlockIdx);
                startProcessingFromBlockIdx++;
            }
        }
    }

    private void increaseProcessedBlockKV(final int height) {
        final KeyValue processedBlocks = getProcessedBlockKV();
        processedBlocks.setValue("" + height);
        keyValueDao.saveOrUpdate(processedBlocks);
    }

    private void processTxs(List<String> txHashes, int blockHeight) {
        int i = 0;
        final Set<String> failedTxs = new HashSet<String>();

        for (String txHash : txHashes) {
            try {
                i++;
                final Map tx = client.getTx(txHash);
                //                log.info("tx idx " + i + ": " + txHash);
                //                log.info("tx ALL: " + tx);
                findAddressedMatch(tx, blockHeight);
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

    private void findAddressedMatch(Map tx, int blockHeight) {
        final List<Map> vouts = (List) tx.get("vout");
        final Long time = (Long) tx.get("time");
        //        System.out.println("vouts: " + vouts);
        final String txid = (String) tx.get("txid");
        log.info("Examining transaction: " + txid + " seen in block: " + blockHeight);
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
                log.warn("We do not support vouts with more than one addresses: " + addresses + ", txid: " + txid);
            }
        }

        if (addressMapping.size() > 0) {
            final List<Address> addresses = addressDao.getByBtcAddresses(addressMapping.keySet());
            for (Address address : addresses) {
                final String btcAddress = address.getBtcAddress();
                final AddressMapping am = addressMapping.get(btcAddress);
                log.info("btcAddress found to notify: " + btcAddress);
                final String transactionIdAndN = txid + ":" + am.n;
                verifyDepositUniqunessAndCreate(blockHeight, time, numberConfirmations, address, am, transactionIdAndN);
            }
        } else {
            log.warn("No addresses gathered for txid: " + txid);
        }

    }

    @Transactional(readOnly = false)
    private void verifyDepositUniqunessAndCreate(int blockHeight, final Long time, final Long numberConfirmations, Address address, final AddressMapping am, final String transactionIdAndN) {
        if (null == depositDao.getByTransactionIdAndN(transactionIdAndN)) {
            final BigInteger amount = convertToLongAmount(am.value);
            log.info("value: " + am.value + "; amount: " + amount + ", transactionIdAndN: " + transactionIdAndN);
            final Deposit dep = new Deposit();
            dep.setAmount(amount);
            dep.setAddress(address);
            dep.setTransactionIdAndN(transactionIdAndN);
            dep.setTime(time);
            dep.setSeenInBlock(blockHeight);
            dep.setNumberConfirmations(numberConfirmations);
            depositDao.create(dep);
            depositDao.flush(dep);
            queueHelper.queueNewDepositToProcess(dep);
        } else {
            log.warn("We already processed transaction for transactionIdAndN: " + transactionIdAndN);
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

    /*package scope*/void setKeyValueDao(KeyValueDao keyValueDao) {
        this.keyValueDao = keyValueDao;
    }

    /*package scope*/void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

}
