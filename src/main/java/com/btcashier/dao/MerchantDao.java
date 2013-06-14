package com.btcashier.dao;

import org.springframework.transaction.annotation.Transactional;

import com.btcashier.domain.Merchant;
import com.btcashier.domain.Sale;

@Transactional(readOnly = true)
public interface MerchantDao extends AbstractJpaDao<Integer, Merchant> {
    
    Merchant getBySale(Sale sale);

}