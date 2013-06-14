package com.btcashier.domain.enums;


public enum SaleStatus {
    
    /**
     * Waiting for all necessary deposits.
     */
    PENDING,
    
    /**
     * Sufficient amount was deposited to the corresponding address.
     */
    COMPLETED,
    
    /**
     * Don't know what kind of errors might trigger this state.
     */
    ERROR

}
