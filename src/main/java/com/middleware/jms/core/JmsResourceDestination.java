package com.middleware.jms.core;

import com.middleware.jms.annotations.JmsDestination;
import com.middleware.jms.core.destination.type.DestinationNamer;
import com.middleware.jms.core.destination.type.DestinationType;

import javax.jms.Destination;



public class JmsResourceDestination {

    private JmsDestination jmsDestination;
    private Destination destination;

    public JmsResourceDestination(Destination destination, JmsDestination jmsDestination) {
        this.destination = destination;
        this.jmsDestination = jmsDestination;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public String getDestinationName() {
        return DestinationNamer.getDestinationSuffixName(jmsDestination);
    }

    public String getTopicId() {
        return jmsDestination.id();
    }

    public DestinationType getDestinationType() {
        return jmsDestination.destinationType();
    }

}
