package com.middleware.jms.resources.listener;

import com.middleware.jms.annotations.JmsListener;
import com.middleware.jms.core.resource.listener.JmsResourceListener;
import org.apache.log4j.Logger;

import java.util.Properties;

@JmsListener(value = DurableProducerListener.class)
public class JmsProducerListenerDurable implements JmsResourceListener {

    private Logger logger = Logger.getLogger(JmsProducerListenerDurable.class);

    @Override
    public void onBeforeProcessingMessage(Properties properties) {
        logger.info("[PRODUCER DURABLE] Specific listener called");
    }
}
