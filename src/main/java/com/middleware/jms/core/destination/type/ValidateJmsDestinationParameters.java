package com.middleware.jms.core.destination.type;

import com.middleware.jms.annotations.JmsDestination;

public class ValidateJmsDestinationParameters implements DestinationTypeFunctionParameters {

    private JmsDestination jmsDestination;

    public ValidateJmsDestinationParameters(JmsDestination jmsDestination) {
        this.jmsDestination = jmsDestination;
    }

    public JmsDestination getJmsDestination() {
        return jmsDestination;
    }
}
