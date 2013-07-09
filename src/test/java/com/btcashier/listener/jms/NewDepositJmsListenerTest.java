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
package com.btcashier.listener.jms;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.btcashier.domain.Address;
import com.btcashier.domain.Deposit;
import com.btcashier.domain.Sale;
import com.btcashier.domain.enums.Currency;
import com.btcashier.domain.enums.SaleStatus;
import com.fasterxml.jackson.core.JsonProcessingException;


public class NewDepositJmsListenerTest {
    
    private DepositsChangeJmsListener listener;
    private Sale sale;
    private List<Deposit> deposits;

    @Before
    public void setUp() throws Exception {
        listener = new DepositsChangeJmsListener();
        sale = new Sale();
        sale.setCurrency(Currency.BTC);
        sale.setDescription("Desc 1");
        sale.setId(3486);
        sale.setPrice(BigInteger.valueOf(123763980));
        sale.setStatus(SaleStatus.PENDING);
        sale.setAddress(new Address("32542fadef32435=="));
        deposits = new ArrayList<>();
        final Deposit dep = new Deposit();
        deposits.add(dep);
        dep.setAmount(BigInteger.valueOf(534534634));
        dep.setId(4324);
        dep.setNumberConfirmations(4L);
        dep.setTime(new Date().getTime());
        dep.setTransactionIdAndNAndBlockTime("ertefg2345fsdfwertwer:3");
    }

    @Test
    public void testCreateJson() throws JsonProcessingException {
        final String json = listener.createJson(sale, deposits, BigInteger.valueOf(100), BigInteger.valueOf(59), BigInteger.ZERO);
        
        // TODO: add assertions
        
    }

}
