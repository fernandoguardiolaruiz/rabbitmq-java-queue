package com.middleware.jms.core.resource.consumer.creator;

import com.middleware.jms.core.destination.type.params.MessageDestinationCreationParameters;
import com.middleware.jms.core.destination.type.DestinationTypeFunctionResult;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;

public interface MessageDestinationCreator<P extends MessageDestinationCreationParameters> extends DestinationTypeFunctionResult {

    MessageConsumer createMessageConsumer(P consumerCreationParameters, String messageSelector) throws JMSException;

}
