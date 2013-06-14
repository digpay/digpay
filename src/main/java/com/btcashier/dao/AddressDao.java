package com.btcashier.dao;

import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.btcashier.domain.Address;
import com.btcashier.domain.Merchant;

@Transactional(readOnly = true)
public interface AddressDao extends AbstractJpaDao<Integer, Address> {
    
    public Address getAddressForMerchantAndBtcAddress(Merchant merchant, String btcAddress);
    
    public Set<Address> getAddressToMonitor();
    
    public Address getByBtcAddress(String btcAddress);
    
    public List<Address> getByBtcAddresses(Set<String> btcAddresses);
    

}