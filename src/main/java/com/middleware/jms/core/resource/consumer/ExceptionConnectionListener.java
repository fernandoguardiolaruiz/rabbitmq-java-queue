package com.middleware.jms.core.resource.consumer;

import org.apache.log4j.Logger;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ExceptionConnectionListener implements ExceptionListener {

    private Logger logger = Logger.getLogger(ExceptionConnectionListener.class);
    private JmsConsumerResource consumerResource;

    public ExceptionConnectionListener(JmsConsumerResource consumerResource) {

        this.consumerResource = consumerResource;
    }

    @Override
    public void onException(JMSException ex) {

        logger.error("Error on connection for resource " + consumerResource.getClass().getName(), ex);
        try {
            CompletableFuture.supplyAsync(() -> {
                consumerResource.stop(true);
                return null;
            }).get(3000, TimeUnit.MILLISECONDS);

        } catch (Exception iex) {
            logger.error("Can't stop " + consumerResource.getClass().getSimpleName(), iex);
        }
        consumerResource.start(true);
    }
}
