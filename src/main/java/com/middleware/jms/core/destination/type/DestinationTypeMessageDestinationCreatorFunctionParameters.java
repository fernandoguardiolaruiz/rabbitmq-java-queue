package com.middleware.jms.core.destination.type;

import com.middleware.jms.core.JmsResourceDestination;

import javax.jms.Session;

public class DestinationTypeMessageDestinationCreatorFunctionParameters implements DestinationTypeFunctionParameters {

    private Session session;
    private JmsResourceDestination jmsResourceDestination;

    public DestinationTypeMessageDestinationCreatorFunctionParameters(Session session, JmsResourceDestination jmsResourceDestination) {
        this.session = session;
        this.jmsResourceDestination = jmsResourceDestination;
    }

    public Session getSession() {
        return session;
    }

    public JmsResourceDestination getJmsResourceDestination() {
        return jmsResourceDestination;
    }
}
