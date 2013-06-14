package com.btcashier.utils;


public class BtcashierException extends RuntimeException {

    private static final long serialVersionUID = 1534647654734L;

    private Integer errorCode;
    
    private String errorMsg;
    
    public BtcashierException(Integer errorCode) {
        this.errorCode = errorCode;
    }
    
    public BtcashierException(Integer errorCode, Throwable t) {
        super(t);
        this.errorCode = errorCode;
    }
    
    public Integer getErrorCode() {
        return errorCode;
    }

    
    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

}
