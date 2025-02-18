package com.middleware.jms.resources.topic;

import com.middleware.jms.annotations.JmsConsumer;
import com.middleware.jms.annotations.JmsDestination;
import com.middleware.jms.core.destination.type.DestinationType;
import com.middleware.jms.core.resource.consumer.JmsConsumerResource;
import com.middleware.jms.connection.JmsConnection;
import com.middleware.jms.core.JmsResourceDestination;
import com.middleware.jms.core.JmsSessionParameters;
import com.middleware.jms.message.TestingMessage;
import org.apache.commons.pool2.ObjectPool;
import org.apache.log4j.Logger;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

@JmsConsumer
@JmsDestination(name = "information-es", destinationType = DestinationType.QUEUE)
public class JmsConsumerES extends JmsConsumerResource<TestingMessage> {

    private Logger logger = Logger.getLogger(JmsConsumerES.class);
    private String messageReceived = null;
    private int totalReceived = 0;

    public JmsConsumerES(ObjectPool<JmsConnection> connectionPool, JmsSessionParameters jmsSessionParameters, JmsResourceDestination jmsResourceDestination, Class<TestingMessage> clazz) {
        super(connectionPool, jmsSessionParameters, jmsResourceDestination, clazz);
    }

    public void process(TestingMessage testingMessage, Properties properties) {
        logger.info("Message Received " + testingMessage.getMessage()+"-ES");
        messageReceived = testingMessage.getMessage();
        totalReceived++;
    }

    public void waitUntilMessageReceived(int waitFor) throws Exception {
        while (waitFor != totalReceived) {
            Thread.sleep(300);
        }
    }


}
