package com.middleware.jms.resources.listener;

import com.middleware.jms.annotations.JmsListener;
import com.middleware.jms.core.resource.listener.JmsResourceListener;
import org.apache.log4j.Logger;

import java.util.Properties;

@JmsListener(value = DurableConsumerListener.class)
public class JmsConsumerListenerDurable implements JmsResourceListener {

    private Logger logger = Logger.getLogger(JmsConsumerListenerDurable.class);

    @Override
    public void onBeforeProcessingMessage(Properties properties) {
        logger.info("[CONSUMER DURABLE] Specific listener called");
    }
}
