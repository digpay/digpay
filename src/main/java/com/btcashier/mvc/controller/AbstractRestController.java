package com.btcashier.mvc.controller;


import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.btcashier.utils.BtcashierException;
import com.btcashier.utils.FailureResult;

public class AbstractRestController {

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public FailureResult handleException(Throwable e, javax.servlet.http.HttpServletResponse response) {
        final FailureResult failureResult;
        if (e instanceof BtcashierException) {
            final BtcashierException be = (BtcashierException) e;
            failureResult = new FailureResult(be);
        } else {
            failureResult = new FailureResult(-1);
            failureResult.setErrorMsg(e.getMessage());
        }
        return failureResult;
    }
    
}
