package com.middleware.jms.core.destination.type.params;

import com.middleware.jms.core.JmsResourceDestination;

import javax.jms.Queue;
import javax.jms.Session;

public class QueueDestinationCreationParameters extends MessageDestinationCreationParameters<Queue> {

    public QueueDestinationCreationParameters(JmsResourceDestination jmsResourceDestination, Session session) {
        super(jmsResourceDestination, session);
    }
}
