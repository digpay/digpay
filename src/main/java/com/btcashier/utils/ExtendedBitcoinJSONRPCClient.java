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
