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
package com.btcashier.service;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.btcashier.utils.AbstractBtcTest;

public class BtcServiceTest extends AbstractBtcTest {

    private static final int MID_TEST = -1;

    private BtcService service;

    @Before
    public void setUp() throws Exception {
        service = new BtcService();
        service.setBtc(getClient());
    }

    @Test
    public void testGenerateNewAddress() {
        final String generatedAddress1 = service.generateNewAddress(MID_TEST);
        final String generatedAddress2 = service.generateNewAddress(MID_TEST);

        Assert.assertThat(generatedAddress1, is(notNullValue()));
        Assert.assertThat(generatedAddress1, is(not(generatedAddress2)));
    }

    public BtcServiceTest() throws Exception {
    }

}
