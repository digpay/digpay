package com.btcashier.listener.jms;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.btcashier.dao.DepositDao;
import com.btcashier.dao.MerchantDao;
import com.btcashier.dao.SaleDao;
import com.btcashier.domain.Deposit;
import com.btcashier.domain.Merchant;
import com.btcashier.domain.Sale;
import com.btcashier.domain.enums.SaleStatus;
import com.btcashier.utils.json.DepositsJsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

public class NewDepositJmsListener implements MessageListener {

    private final static Logger log = Logger.getLogger(NewDepositJmsListener.class);

    @Autowired
    private DepositDao depositDao;

    @Autowired
    private SaleDao saleDao;

    @Autowired
    private MerchantDao merchantDao;

    public void onMessage(Message message) {
        if (message instanceof ObjectMessage) {
            try {
                final ObjectMessage om = (ObjectMessage) message;
                final Object obj = om.getObject();
                if (!(obj instanceof Integer)) {
                    final String msg = "Object in the JMS message must be of type Integer, but was: " + (null == obj ? "[NULL]" : obj.getClass());
                    log.error(msg);
                    throw new IllegalArgumentException(msg);
                }
                final Integer depId = (Integer) obj;
                log.info("Received JMS message with new deposit id to process: " + depId);

                final Deposit deposit = depositDao.findById(depId);
                if (null == deposit) {
                    final String msg = "Deposit not found in DB for id: " + depId;
                    log.error(msg);
                    throw new IllegalArgumentException(msg);
                }

                final Sale sale = saleDao.getByDeposit(deposit);
                if (null == sale) {
                    final String msg = "Can not find coresponding Sale to the depositID: " + deposit.getId();
                    log.error(msg);
                    throw new IllegalArgumentException(msg);
                }

                sale.setLastSentDate(new Date());
                final List<Deposit> allDepositsForSale = depositDao.getAllBySaleOrderedByDateDesc(sale);
                if (null == allDepositsForSale || allDepositsForSale.size() == 0) {
                    final String msg = "Can not find any coresponding Deposits for saleID: " + sale.getId();
                    log.error(msg);
                    throw new IllegalArgumentException(msg);
                }

                final Merchant merchant = merchantDao.getBySale(sale);
                if (null == merchant) {
                    final String msg = "Can not find merchant for saleID: " + sale.getId();
                    log.error(msg);
                    throw new IllegalArgumentException(msg);
                }

                // calculate total amount with enough confirmations:
                final Integer requiredConfirmations = merchant.getRequiredConfirmations();
                BigInteger totalAmount = BigInteger.ZERO;
                BigInteger totalConfirmedAmount = BigInteger.ZERO;
                for (Deposit d : allDepositsForSale) {
                    totalAmount = totalAmount.add(d.getAmount());
                    if (d.getNumberConfirmations() >= requiredConfirmations) {
                        totalConfirmedAmount = totalConfirmedAmount.add(d.getAmount());
                    }
                }

                if (totalAmount.compareTo(sale.getPrice()) >= 0) {
                    // there is enough confirmed deposits:
                    sale.setStatus(SaleStatus.COMPLETED);
                }
                
                saleDao.saveOrUpdate(sale);

                final String json = createJson(sale, allDepositsForSale, totalAmount, totalConfirmedAmount);
                final String url = merchant.getUrl();

                try {
                    log.info("Trying to call merchant url: " + url + ", json: " + json);
                    callMerchant(json, url);
                } catch (IOException | InterruptedException | ExecutionException ex) {
                    log.error("Error while calling merchant url: " + url + " with json: " + json, ex);
                    throw new RuntimeException(ex);
                }

            } catch (JMSException ex) {
                log.error("Error while processing JMS message", ex);
                throw new RuntimeException(ex);
            } catch (JsonProcessingException ex) {
                log.error("Error while creating JSON response", ex);
                throw new RuntimeException(ex);
            }
        } else {
            final String msg = "Message must be of type ObjectMessage, but was: " + (null == message ? "[NULL]" : message.getClass());
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    private void callMerchant(final String json, final String url) throws IOException, InterruptedException, ExecutionException {
        try (final AsyncHttpClient client = new AsyncHttpClient()) {
            final Response resp = client.preparePost(url).setBody(json).execute(new AsyncCompletionHandler<Response>() {
                @Override
                public Response onCompleted(Response response) throws Exception {
                    log.info("Call to merchant finished successfully with response: " + response + ", url: " + url);
                    
                    // TODO: we are interested in response from the merchant, if it will not come or will be ERROR we will try to retry message (not implemented yet)
                    
                    return response;
                }
            }).get();
            log.info("Response received from merchant: " + resp.getStatusCode() + ", body: " + resp.getResponseBody());
        }
    }

    /*package scope*/String createJson(Sale sale, List<Deposit> allDepositsForSale, BigInteger totalAmount, BigInteger totalConfirmedAmount) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        mapper.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
        mapper.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
        mapper.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);

        final Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("sale", sale);
        jsonMap.put("deposits_count", allDepositsForSale.size());
        jsonMap.put("last_10_deposits", createSubList(allDepositsForSale, 10));
        jsonMap.put("total_amount", totalAmount);
        jsonMap.put("total_confirmed_amount", totalConfirmedAmount);

        return mapper.writerWithView(DepositsJsonView.class).writeValueAsString(jsonMap);
    }

    private <T> List<T> createSubList(List<T> l, int max) {
        if (l.size() <= max) {
            return l;
        } else {
            return l.subList(0, max);
        }
    }

}