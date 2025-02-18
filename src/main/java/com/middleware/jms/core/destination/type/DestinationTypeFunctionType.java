package com.middleware.jms.core.destination.type;

import com.middleware.jms.core.resource.consumer.creator.MessageDestinationCreator;
import com.middleware.jms.core.destination.type.params.MessageDestinationCreationParameters;

public enum DestinationTypeFunctionType {

    VALIDATE_JMS_DESTINATION(Boolean.class),
    CREATE_MESSAGE_CONSUMER_PARAMETERS(MessageDestinationCreationParameters.class),
    CREATE_MESSAGE_CONSUMER(MessageDestinationCreator.class);

    public Class clazzReturned;

    DestinationTypeFunctionType(Class clazzReturned) {
        this.clazzReturned = clazzReturned;
    }

}
