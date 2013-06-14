package com.btcashier.dao.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.btcashier.dao.AddressDao;
import com.btcashier.domain.Address;
import com.btcashier.domain.Merchant;

@Repository("addressDao")
public class AddressDaoImpl extends AbstractJpaDaoImpl<Integer, Address> implements AddressDao {

    @Override
    public Address getAddressForMerchantAndBtcAddress(Merchant merchant, String btcAddress) {
        final Query q = em.createQuery("select a from Address a where a.merchant = :merchant and a.btcAddress = :btcAddress", Address.class);
        q.setParameter("merchant", merchant);
        q.setParameter("btcAddress", btcAddress);
        final List<Address> addresses = q.getResultList();
        return obtainFirst(addresses);
    }

    @Override
    public Set<Address> getAddressToMonitor() {
        // TODO: think about maybe a little bit more efficient way, eg to return only those addresses that are in non-final state (no ERROR/FINISHED)?
        return new HashSet<Address>(findAll());
    }

    @Override
    public Address getByBtcAddress(String btcAddress) {
        final List<Address> addresses = getByBtcAddresses(new HashSet<String>(Arrays.asList(btcAddress)));
        return obtainFirst(addresses);
    }

    @Override
    public List<Address> getByBtcAddresses(Set<String> btcAddresses) {
        final Query q = em.createQuery("select a from Address a where a.btcAddress in (:btcAddresses)", Address.class);
        q.setParameter("btcAddresses", btcAddresses);
        final List<Address> addresses = q.getResultList();
        return addresses;
    }

}
