package com.middleware.jms.core.resource.consumer.creator;

import com.middleware.jms.core.destination.type.params.DurableSubscriberCreationParameters;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;

public class DurableSubscriberCreator implements MessageDestinationCreator<DurableSubscriberCreationParameters> {

    @Override
    public MessageConsumer createMessageConsumer(DurableSubscriberCreationParameters durableSubscriberCreationParameters, String messageSelector) throws JMSException {
        return durableSubscriberCreationParameters.getSession().createDurableSubscriber(durableSubscriberCreationParameters.getDestination(), durableSubscriberCreationParameters.getTopicId());
    }
}
