package com.middleware.jms.resources.queue.durable;

import com.middleware.jms.core.destination.type.DestinationSuffix;

public class EnvironmentSuffix implements DestinationSuffix {


    @Override
    public String version() {
        return "DEV";
    }
}
