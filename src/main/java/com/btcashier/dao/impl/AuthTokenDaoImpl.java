package com.btcashier.dao.impl;

import org.springframework.stereotype.Repository;

import com.btcashier.dao.AuthTokenDao;
import com.btcashier.domain.AuthToken;

@Repository("authTokenDao")
public class AuthTokenDaoImpl extends AbstractJpaDaoImpl<String, AuthToken> implements AuthTokenDao {

}
