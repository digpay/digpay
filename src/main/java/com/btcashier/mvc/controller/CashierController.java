package com.btcashier.mvc.controller;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.btcashier.dao.AddressDao;
import com.btcashier.dao.DepositDao;
import com.btcashier.dao.MerchantDao;
import com.btcashier.dao.SaleDao;
import com.btcashier.domain.Address;
import com.btcashier.domain.AuthToken;
import com.btcashier.domain.Deposit;
import com.btcashier.domain.Merchant;
import com.btcashier.domain.Sale;
import com.btcashier.domain.enums.Currency;
import com.btcashier.domain.enums.SaleStatus;
import com.btcashier.service.AuthTokenService;
import com.btcashier.service.BtcService;
import com.btcashier.utils.BtcashierException;
import com.btcashier.utils.ErrorCodes;
import com.btcashier.utils.FailureResult;

@Controller
@RequestMapping("/cashier")
public class CashierController {

    private static final Logger log = Logger.getLogger(CashierController.class);

    @Autowired
    private MerchantDao merchantDao;

    @Autowired
    private SaleDao saleDao;
    
    @Autowired
    private AddressDao addressDao;
    
    @Autowired
    private DepositDao depositDao;

    @Autowired
    private BtcService btcService;

    @Autowired
    private AuthTokenService authTokenService;
    
    /**
     */
    @RequestMapping(value = "/verify/{mid}/{token}/{btcAddress}", method = RequestMethod.GET)
    public ModelAndView verifyTransaction(@PathVariable Integer mid, @PathVariable String token, @PathVariable String btcAddress) {
        validateTokenAndMid(mid, token);

        final Merchant merchant = merchantDao.findById(mid);
        validateMerchant(merchant);
        
        validateMerchantAndBtcAddress(merchant, btcAddress);
        
        final Address address = addressDao.getByBtcAddress(btcAddress);
        final List<Deposit> deposits = depositDao.getAllForAddress(address);
        
        final ModelAndView mv = new ModelAndView("verify");
        mv.addObject("btcAddress", btcAddress);
        mv.addObject("token", token);
        mv.addObject("deposits", deposits);

        return mv;
    }

    private void validateMerchantAndBtcAddress(Merchant merchant, String btcAddress) {
        final Address address = addressDao.getAddressForMerchantAndBtcAddress(merchant, btcAddress);
        if (null == address) {
            throw ErrorCodes.create(ErrorCodes.CASHIER_ADDRESS_DOES_NOT_CORRECT);
        }
    }

    /**
     * Actual path will be something like this:
     * /1/43243452345345;currency=BTC;price=123;description=Car Ferrari;merchantSaleId=ABC123
     * 
     * @param mid
     * @param token
     * @param matrixVars
     * @return
     */
    @RequestMapping(value = "/enter/{mid}/{token}", method = RequestMethod.GET)
    public ModelAndView enterCashier(@PathVariable Integer mid, @PathVariable String token, @MatrixVariable Currency currency, @MatrixVariable String description, @MatrixVariable String merchantSaleId, @MatrixVariable Long price) {
        validateTokenAndMid(mid, token);

        final Merchant merchant = merchantDao.findById(mid);
        validateMerchantAndMerchantSaleId(merchant, merchantSaleId);

        final String btcAddress = btcService.generateNewAddress(mid);

        final Sale sale = new Sale();
        sale.setDescription(description);
        sale.setCurrency(currency);
        sale.setMerchantSaleId(merchantSaleId);
        sale.setPrice(BigInteger.valueOf(price));
        sale.setMerchant(merchant);
        sale.setStatus(SaleStatus.PENDING);
        final Address address = new Address();
        sale.setAddress(address);
        address.setBtcAddress(btcAddress);
        address.setMerchant(merchant);
        address.setSale(sale);
        saleDao.saveOrUpdate(sale);

        final ModelAndView mv = new ModelAndView("cashier");
        mv.addObject("btcAddress", btcAddress);
        mv.addObject("sale", sale);
        mv.addObject("token", token);

        return mv;
    }
    
    private void validateMerchant(final Merchant merchant) {
        if (null == merchant) {
            throw ErrorCodes.create(ErrorCodes.CASHIER_MERCHANT_DOES_NOT_EXIST);
        }
    }

    private void validateMerchantAndMerchantSaleId(final Merchant merchant, String merchantSaleId) {
        validateMerchant(merchant);
        final Sale saleByMerchantSaleId = saleDao.getByMerchantAndMerchantSaleId(merchant, merchantSaleId);
        if (null != saleByMerchantSaleId) {
            throw ErrorCodes.create(ErrorCodes.CASHIER_MERCHANT_SALE_ID_NOT_UNIQUE);
        }
    }

    private void validateTokenAndMid(Integer mid, String token) {
        final AuthToken authToken = authTokenService.getAuthToken(token);
        if (null == authToken || !authToken.getMerchant().getMid().equals(mid)) {
            throw ErrorCodes.create(ErrorCodes.AUTH_MID_OR_TOKEN_WRONG);
        }
    }

    /**
     * TODO: change into web-based view
     */
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public FailureResult handleException(Throwable e, javax.servlet.http.HttpServletResponse response) {
        log.error("Cashier.handleException", e);
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
