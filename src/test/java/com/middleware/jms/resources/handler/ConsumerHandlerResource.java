package com.middleware.jms.resources.handler;

import com.middleware.jms.annotations.JmsHandler;
import com.middleware.jms.core.resource.handler.JmsHandlerResource;
import org.apache.log4j.Logger;

import javax.jms.JMSException;
import java.util.Properties;

@JmsHandler(ConsumerHandler.class)
public class ConsumerHandlerResource<T> extends JmsHandlerResource<String, T> {

    private Logger logger = Logger.getLogger(ConsumerHandlerResource.class);

    @Override
    public String handleBeforeProcessingMessage(T message, Properties properties) throws JMSException {
        String handlingMessage = "Iniciando session de DB";
        logger.debug("handleBeforeConsumingMessage " + handlingMessage);
        return handlingMessage;
    }


    public void handleFinallyConsumingMessage(String handlingMessage) {
        logger.debug("handleFinallyConsumingMessage " + handlingMessage);
    }
}
