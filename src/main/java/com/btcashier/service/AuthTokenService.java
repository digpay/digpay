package com.btcashier.service;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.btcashier.dao.AuthTokenDao;
import com.btcashier.domain.AuthToken;
import com.btcashier.domain.Merchant;


@Service
public class AuthTokenService {
    
    @Autowired
    public AuthTokenDao authTokenDao;
    
    public AuthToken getAuthToken(String tokenId) {
        return authTokenDao.findById(tokenId);
    }
    
    public AuthToken createNewToken(Merchant merchant) {
        final AuthToken token = new AuthToken();
        token.setMerchant(merchant);
        token.setToken(UUID.randomUUID().toString());
        token.setCreatedAt(new Date());
        authTokenDao.saveOrUpdate(token);
        return token;
    }


}
