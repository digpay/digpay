package com.btcashier.dao;

import org.springframework.transaction.annotation.Transactional;

import com.btcashier.domain.Deposit;
import com.btcashier.domain.Merchant;
import com.btcashier.domain.Sale;

@Transactional(readOnly = true)
public interface SaleDao extends AbstractJpaDao<Integer, Sale> {
    
    Sale getByDeposit(Deposit deposit);
    
    Sale getByMerchantAndMerchantSaleId(Merchant merchant, String merchantSaleId);
    

}