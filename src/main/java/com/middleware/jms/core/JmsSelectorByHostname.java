package com.middleware.jms.core;

import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.util.Properties;

public class JmsSelectorByHostname implements JmsSelector {

    private final Logger logger = Logger.getLogger(JmsSelectorByHostname.class);

    public Properties properties() {
        Properties properties = null;
        try {
            properties = new Properties();
            properties.put("Host", InetAddress.getLocalHost().getHostName());
        } catch (Exception e) {
            logger.warn("Cant't create properties ", e);
        }
        return properties;
    }

}
