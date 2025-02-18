package com.middleware.jms.resources.listener;

import com.middleware.jms.annotations.JmsListener;
import com.middleware.jms.core.resource.listener.JmsResourceListener;
import org.apache.log4j.Logger;

import java.util.Properties;

@JmsListener
public class JmsListenerAll implements JmsResourceListener {

    private Logger logger = Logger.getLogger(JmsListenerAll.class);

    @Override
    public void onBeforeProcessingMessage(Properties properties) {
        logger.info("[ALL] Listener on before process message");
    }
}
