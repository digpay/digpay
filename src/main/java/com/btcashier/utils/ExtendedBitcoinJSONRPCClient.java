package com.btcashier.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.azazar.bitcoin.jsonrpcclient.BitcoinException;
import com.azazar.bitcoin.jsonrpcclient.BitcoinJSONRPCClient;


public class ExtendedBitcoinJSONRPCClient extends BitcoinJSONRPCClient {

    public ExtendedBitcoinJSONRPCClient() {
    }

    public ExtendedBitcoinJSONRPCClient(String rpcUrl) throws MalformedURLException {
        super(rpcUrl);
    }

    public ExtendedBitcoinJSONRPCClient(URL rpc) {
        super(rpc);
    }

    public ExtendedBitcoinJSONRPCClient(boolean testNet) {
        super(testNet);
    }
    
    public Map getTx(String txHash) throws BitcoinException {
        return (Map) query("getrawtransaction", txHash, 1);
    }

    public String getBlockHash(int blockIdx) throws BitcoinException {
        return (String) query("getblockhash", blockIdx);
    }

}
