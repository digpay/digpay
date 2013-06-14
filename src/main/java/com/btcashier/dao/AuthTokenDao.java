package com.btcashier.dao;

import org.springframework.transaction.annotation.Transactional;

import com.btcashier.domain.AuthToken;

@Transactional(readOnly = true)
public interface AuthTokenDao extends AbstractJpaDao<String, AuthToken> {
    

}