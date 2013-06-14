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
