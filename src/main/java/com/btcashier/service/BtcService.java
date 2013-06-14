package com.btcashier.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.azazar.bitcoin.jsonrpcclient.BitcoinException;
import com.azazar.bitcoin.jsonrpcclient.BitcoinJSONRPCClient;
import com.btcashier.utils.ErrorCodes;


@Service
public class BtcService {
    
    private static final Logger log = Logger.getLogger(BtcService.class);
    
    @Autowired
    private BitcoinJSONRPCClient btc;
    
    public String generateNewAddress(Integer mid) {
        final String btcAddress;
        try {
            btcAddress = btc.getNewAddress(mid.toString());
        } catch (BitcoinException e) {
            log.error("Unable to create new BTC address", e);
            throw ErrorCodes.create(ErrorCodes.BTC_UNABLE_TO_CREATE_NEW_ADDRESS, e);
        }
        return btcAddress;
    }

    
    /*package scope*/void setBtc(BitcoinJSONRPCClient btc) {
        this.btc = btc;
    }
    
}
