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
package com.btcashier.utils;

public class ErrorCodes {

    public static final int AUTH_MID_OR_API_KEY_WRONG = -1001;
    public static final int AUTH_MID_OR_TOKEN_WRONG = -1002;
    
    public static final int CASHIER_NOT_SUITABLE_NUMBER_OF_ITEMS_AND_PRICES = -2001;
    public static final int CASHIER_NON_INTEGER_PRICE = -2002;
    public static final int CASHIER_MERCHANT_DOES_NOT_EXIST = -2003;
    public static final int CASHIER_ADDRESS_DOES_NOT_CORRECT = -2004;
    public static final int CASHIER_MERCHANT_SALE_ID_NOT_UNIQUE = -2005;
    
    public static final int BTC_UNABLE_TO_CREATE_NEW_ADDRESS = -3001;

    public static BtcashierException create(int errorCode) {
        return create(errorCode, null);
    }
    
    public static BtcashierException create(int errorCode, Throwable t) {
        return new BtcashierException(errorCode, t);
    }

    private ErrorCodes() {
    }

}
