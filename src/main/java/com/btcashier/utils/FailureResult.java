package com.btcashier.utils;


public class FailureResult {
    
    private Integer errorCode;
    
    private String errorMsg;
    
    public FailureResult(Integer errorCode) {
        this.errorCode = errorCode;
    }
    
    public FailureResult(BtcashierException be) {
        errorCode = be.getErrorCode();
        errorMsg = be.getErrorMsg();
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
