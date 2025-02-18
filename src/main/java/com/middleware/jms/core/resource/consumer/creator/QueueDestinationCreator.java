package com.middleware.jms.core.resource.consumer.creator;

import com.middleware.jms.core.destination.type.params.QueueDestinationCreationParameters;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;

public class QueueDestinationCreator implements MessageDestinationCreator<QueueDestinationCreationParameters> {

    @Override
    public MessageConsumer createMessageConsumer(QueueDestinationCreationParameters queueConsumerCreationParameters, String messageSelector) throws JMSException {
        return queueConsumerCreationParameters.getSession().createConsumer(queueConsumerCreationParameters.getDestination(), messageSelector);
    }
}
