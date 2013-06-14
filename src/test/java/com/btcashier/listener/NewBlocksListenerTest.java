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
package com.btcashier.listener;

import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.math.BigInteger;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.core.task.support.TaskExecutorAdapter;

import com.btcashier.dao.AddressDao;
import com.btcashier.dao.DepositDao;
import com.btcashier.dao.KeyValueDao;
import com.btcashier.domain.KeyValue;
import com.btcashier.domain.enums.Keys;
import com.btcashier.utils.AbstractBtcTest;

public class NewBlocksListenerTest extends AbstractBtcTest {

    private NewBlocksListener listener;
    private static final String BLACK_HASH = "0000000000000155303b637fda8f13f1778f58b64328c95ae201ccffbc5c6a8c";

    @Mock
    private AddressDao addressDao;

    @Mock
    private DepositDao depositDao;

    @Mock
    private KeyValueDao keyValueDao;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        listener = new NewBlocksListener();
        listener.setClient(getClient());
        listener.setAddressDao(addressDao);
        listener.setTaskExecutor(new TaskExecutorAdapter(Executors.newSingleThreadExecutor()));

        when(depositDao.increaseNumberOfConfirmations(anyInt())).thenReturn(0);
        listener.setDepositDao(depositDao);

        final KeyValue kv = new KeyValue(Keys.PROCESSED_BLOCKS);
        kv.setValue("999999999");
        when(keyValueDao.getByKey(Keys.PROCESSED_BLOCKS)).thenReturn(kv);

        listener.setKeyValueDao(keyValueDao);
        listener.init();
    }

    @After
    public void tearDown() throws Exception {
        listener.destroy();
    }

    @Test
    public void testProcessBlock() {
        when(addressDao.getByBtcAddress(anyString())).thenReturn(null);
        listener.processBlock(BLACK_HASH, false);
    }

    @Test
    public void testConvertToLongAmount() {
        final BigInteger amount1 = NewBlocksListener.convertToLongAmount(0.3);
        final BigInteger amount2 = NewBlocksListener.convertToLongAmount(0.1);
        Assert.assertThat(amount1, is(BigInteger.valueOf(30000000)));
        Assert.assertThat(amount2, is(BigInteger.valueOf(10000000)));
    }

    public NewBlocksListenerTest() throws Exception {
    }

}
