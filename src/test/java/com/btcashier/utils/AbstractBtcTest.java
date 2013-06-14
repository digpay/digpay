package com.btcashier.utils;


public abstract class AbstractBtcTest {

    private ExtendedBitcoinJSONRPCClient client;

    public AbstractBtcTest() throws Exception {
        client = new ExtendedBitcoinJSONRPCClient(BtcTestConstants.BTC_URL);
    }

    protected ExtendedBitcoinJSONRPCClient getClient() {
        return client;
    }

}
