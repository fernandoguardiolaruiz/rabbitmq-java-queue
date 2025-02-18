package com.middleware.jms.resources.handler;

import com.middleware.jms.annotations.JmsHandler;
import com.middleware.jms.core.resource.handler.JmsHandlerResource;
import org.apache.log4j.Logger;

import javax.jms.JMSException;
import java.util.Properties;

@JmsHandler(value = ProducerHandler.class)
public class ProducerHandlerResource<T> extends JmsHandlerResource<String, T> {

    private Logger logger = Logger.getLogger(ConsumerHandlerResource.class);

    @Override
    public String handleBeforeProcessingMessage(T message, Properties properties) throws JMSException {
        String handlingMessage = "Iniciando session de DB";
        logger.debug("handleBeforeSendingMessage " + handlingMessage);
        properties.setProperty("ENVIRONMENT","STABLE");
        return handlingMessage;
    }

    @Override
    public void handleFinallyProcessingMessage(String handlingMessage, T t, Properties properties) {
        logger.debug("handleFinallySendingMessage " + handlingMessage);
    }

}
