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

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.btcashier.dao.SaleDao;
import com.btcashier.domain.Deposit;
import com.btcashier.domain.Merchant;
import com.btcashier.domain.Sale;

@Repository("saleDao")
public class SaleDaoImpl extends AbstractJpaDaoImpl<Integer, Sale> implements SaleDao {

    @Override
    public Sale getByDeposit(Deposit deposit) {
        final Query q = em.createQuery("select d.address.sale from Deposit d where d = :deposit", Sale.class);
        q.setParameter("deposit", deposit);
        final List<Sale> sales = q.getResultList();
        return obtainFirst(sales);
    }

    @Override
    public Sale getByMerchantAndMerchantSaleId(Merchant merchant, String merchantSaleId) {
        final Query q = em.createQuery("select s from Sale s where s.merchant = :merchant and s.merchantSaleId = :merchantSaleId", Sale.class);
        q.setParameter("merchant", merchant);
        q.setParameter("merchantSaleId", merchantSaleId);
        final List<Sale> sales = q.getResultList();
        return obtainFirst(sales);
    }

}
