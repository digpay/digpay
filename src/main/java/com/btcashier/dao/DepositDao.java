package com.btcashier.dao;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.btcashier.domain.Address;
import com.btcashier.domain.Deposit;
import com.btcashier.domain.Sale;

@Transactional(readOnly = true)
public interface DepositDao extends AbstractJpaDao<Integer, Deposit> {
    
    List<Deposit> getAllForAddress(Address address);
    
    Deposit getByTransactionIdAndN(String transactionIdAndN);
    
    List<Deposit> getAllBySaleOrderedByDateDesc(Sale sale);
    
    
    List<Deposit> getPerSaleUniqueAndWithRequiredOrLessConfirmations();
    
    @Transactional(readOnly = false)
    int increaseNumberOfConfirmations(int blockHeight);
    

}