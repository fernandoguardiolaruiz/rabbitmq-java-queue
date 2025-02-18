package com.middleware.jms.core.resource.consumer.creator;

import com.middleware.jms.core.JmsResourceDestination;
import com.middleware.jms.core.destination.type.DestinationTypeFunctionExecutor;
import com.middleware.jms.core.destination.type.DestinationTypeFunctionType;
import com.middleware.jms.core.destination.type.DestinationTypeMessageDestinationCreatorFunctionParameters;
import com.middleware.jms.core.destination.type.VoidParameters;
import com.middleware.jms.core.destination.type.params.MessageDestinationCreationParameters;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

public class MessageConsumerFactory {

    private DestinationTypeFunctionExecutor destinationTypeFunctionExecutor;

    private MessageConsumerFactory(DestinationTypeFunctionExecutor destinationTypeFunctionExecutor) {
        this.destinationTypeFunctionExecutor = destinationTypeFunctionExecutor;
    }

    public static MessageConsumerFactory getInstance(DestinationTypeFunctionExecutor destinationTypeFunctionExecutor) {
        return new MessageConsumerFactory(destinationTypeFunctionExecutor);
    }

    public MessageConsumer createMesssageConsumer(Session session, JmsResourceDestination jmsResourceDestination, String messageSelector) throws JMSException {
        DestinationTypeMessageDestinationCreatorFunctionParameters durabilityMessageConsumerCreatorFunctionParameters = new DestinationTypeMessageDestinationCreatorFunctionParameters(session, jmsResourceDestination);
        MessageDestinationCreationParameters messageConsumerCreationParameters = (MessageDestinationCreationParameters) destinationTypeFunctionExecutor.execute(DestinationTypeFunctionType.CREATE_MESSAGE_CONSUMER_PARAMETERS, jmsResourceDestination.getDestinationType(), durabilityMessageConsumerCreatorFunctionParameters);
        VoidParameters voidParameters = new VoidParameters();
        MessageDestinationCreator messageConsumerCreator = (MessageDestinationCreator) destinationTypeFunctionExecutor.execute(DestinationTypeFunctionType.CREATE_MESSAGE_CONSUMER, jmsResourceDestination.getDestinationType(), voidParameters);
        return messageConsumerCreator.createMessageConsumer(messageConsumerCreationParameters, messageSelector);
    }

}



