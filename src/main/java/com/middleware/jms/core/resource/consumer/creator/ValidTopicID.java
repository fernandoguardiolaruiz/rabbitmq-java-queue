package com.middleware.jms.core.resource.consumer.creator;

import com.middleware.jms.core.destination.type.DestinationTypeFunctionResult;

public class ValidTopicID implements DestinationTypeFunctionResult {

    private boolean valid;

    public ValidTopicID(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }
}
