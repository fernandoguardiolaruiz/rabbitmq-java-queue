package com.middleware.jms.resources.queue.durable;

import com.middleware.jms.core.JmsSelector;

import java.util.Properties;

public class MessageSelector implements JmsSelector {


    @Override
    public Properties properties() {
        Properties properties = new Properties();
        properties.setProperty("ENVIRONMENT", "DEV");
        return properties;
    }
}
