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
