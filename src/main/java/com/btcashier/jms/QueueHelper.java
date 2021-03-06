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
    
    private static final String QUEUE_NAME_DEPOSITS_CHANGE = "Q_DEPOSITS_CHANGE";

    private final static Logger log = Logger.getLogger(QueueHelper.class);
    
    @Autowired
    private JmsTemplate jmsTemplate;
    
    public void queueDepositsChangeToProcess(Deposit dep) {
        final Integer depId = dep.getId();
        log.info("Send JMS notification about new deposit id: " + depId);
        jmsTemplate.send(QUEUE_NAME_DEPOSITS_CHANGE, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                log.info("JMS notification about new deposit almost send, deposit id: " + depId);
                return session.createObjectMessage(depId);
            }
        });
    }


}
