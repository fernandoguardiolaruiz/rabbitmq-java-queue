package com.middleware.jms.core.destination.type.params;

import com.middleware.jms.core.JmsResourceDestination;
import com.middleware.jms.core.destination.type.DestinationTypeFunctionResult;

import javax.jms.Destination;
import javax.jms.Session;


public abstract class MessageDestinationCreationParameters<T extends Destination> implements DestinationTypeFunctionResult {

    private Session session;
    private JmsResourceDestination jmsResourceDestination;

    public MessageDestinationCreationParameters(JmsResourceDestination jmsResourceDestination, Session session) {
        this.jmsResourceDestination = jmsResourceDestination;
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public JmsResourceDestination getJmsResourceDestination() {
        return jmsResourceDestination;
    }

    public void setJmsResourceDestination(JmsResourceDestination jmsResourceDestination) {
        this.jmsResourceDestination = jmsResourceDestination;
    }

    public T getDestination() {
        return (T) jmsResourceDestination.getDestination();
    }
}
