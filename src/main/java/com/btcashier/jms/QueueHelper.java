package com.btcashier.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import com.btcashier.domain.Deposit;


@Component
public class QueueHelper {
    
    private static final String QUEUE_NAME_NEW_DEPOSIT = "Q_NEW_DEPOSIT";

    private final static Logger log = Logger.getLogger(QueueHelper.class);
    
    @Autowired
    private JmsTemplate jmsTemplate;
    
    public void queueNewDepositToProcess(Deposit dep) {
        final Integer depId = dep.getId();
        log.info("Send JMS notification about new deposit id: " + depId);
        jmsTemplate.send(QUEUE_NAME_NEW_DEPOSIT, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                log.info("JMS notification almost send, deposit id: " + depId);
                return session.createObjectMessage(depId);
            }
        });
    }


}
