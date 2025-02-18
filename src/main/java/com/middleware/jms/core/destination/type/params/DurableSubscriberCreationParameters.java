package com.middleware.jms.core.destination.type.params;

import com.middleware.jms.core.JmsResourceDestination;

import javax.jms.Session;
import javax.jms.Topic;

public class DurableSubscriberCreationParameters extends MessageDestinationCreationParameters<Topic> {

    public DurableSubscriberCreationParameters(JmsResourceDestination jmsResourceDestination, Session session) {
        super(jmsResourceDestination, session);
    }

    public String getTopicId() {
        return getJmsResourceDestination().getTopicId();
    }
}
