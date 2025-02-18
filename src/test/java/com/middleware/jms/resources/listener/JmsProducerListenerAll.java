package com.middleware.jms.resources.listener;

import com.middleware.jms.annotations.JmsListener;
import com.middleware.jms.annotations.listener.JmsAllProducers;
import com.middleware.jms.core.resource.listener.JmsResourceListener;
import org.apache.log4j.Logger;

import java.util.Properties;


@JmsListener(value = JmsAllProducers.class, priority = 1)
public class JmsProducerListenerAll implements JmsResourceListener {

    private Logger logger = Logger.getLogger(JmsProducerListenerAll.class);

    @Override
    public void onBeforeProcessingMessage(Properties properties) {
        logger.info("[PRODUCER] Listener before send message");
    }
}
