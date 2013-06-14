/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
