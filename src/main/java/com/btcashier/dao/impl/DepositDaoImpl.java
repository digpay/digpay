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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.btcashier.dao.DepositDao;
import com.btcashier.domain.Address;
import com.btcashier.domain.Deposit;
import com.btcashier.domain.Sale;

@Repository("depositDao")
@Transactional(readOnly = true)
public class DepositDaoImpl extends AbstractJpaDaoImpl<Integer, Deposit> implements DepositDao {

    private final static Logger log = Logger.getLogger(DepositDaoImpl.class);

    @Override
    public List<Deposit> getAllForAddress(Address address) {
        final Query q = em.createQuery("select d from Deposit d where d.address = :address", Deposit.class);
        q.setParameter("address", address);
        return q.getResultList();
    }

    @Override
    public Deposit getByTransactionIdAndN(String transactionIdAndN) {
        final Query q = em.createQuery("select d from Deposit d where d.transactionIdAndN = :transactionIdAndN", Deposit.class);
        q.setParameter("transactionIdAndN", transactionIdAndN);
        final List<Deposit> deposits = q.getResultList();
        return obtainFirst(deposits);
    }

    @Override
    public List<Deposit> getAllBySaleOrderedByDateDesc(Sale sale) {
        final Query q = em.createQuery("select d from Sale s join s.address a join a.deposits d where s = :sale order by d.time desc,d.id desc", Deposit.class);
        q.setParameter("sale", sale);
        final List<Deposit> deposits = q.getResultList();
        return deposits;
    }

    @Override
    public List<Deposit> getPerSaleUniqueAndWithRequiredOrLessConfirmations() {
        final Query q = em.createQuery("select distinct a from Address a join a.deposits d where d.numberConfirmations <= a.merchant.requiredConfirmations", Address.class);
        final List<Address> addresses = q.getResultList();
        log.info("Addresses number: " + addresses.size());

        final List<Deposit> deposits = new ArrayList<>();
        for (Address address : addresses) {
            deposits.add(address.getDeposits().iterator().next());
        }
        log.info("Deposits number: " + deposits.size());
        return deposits;
    }

    @Override
    @Transactional(readOnly = false)
    public int increaseNumberOfConfirmations(final int blockHeight) {
        final Query q1 = em.createQuery("select d2.id from Deposit d2 join d2.address a join a.merchant m where d2.numberConfirmations < m.requiredConfirmations");
        final List<Integer> depositIdsList = q1.getResultList();
        log.info("Deposit ids to update number of confirmations: " + depositIdsList);

        if (depositIdsList.size() > 0) {
            final Query q = em.createQuery("update Deposit set numberConfirmations = 1 + :blockHeight - seenInBlock where id in (:depositIdsList) and numberConfirmations <> 1 + :blockHeight - seenInBlock");
            q.setParameter("blockHeight", blockHeight);
            q.setParameter("depositIdsList", depositIdsList);
            final int updates = q.executeUpdate();
            log.info("Number of updates rows: " + updates);
            return updates;
        } else {
            log.info("No row updated");
            return 0;
        }
    }

}
