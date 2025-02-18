package com.middleware.jms.resources.queue.durable;

import com.middleware.jms.resources.listener.DurableConsumerListener;
import com.middleware.jms.annotations.JmsConsumer;
import com.middleware.jms.annotations.JmsDestination;
import com.middleware.jms.connection.JmsConnection;
import com.middleware.jms.core.JmsResourceDestination;
import com.middleware.jms.core.JmsSessionParameters;
import com.middleware.jms.core.destination.type.DestinationType;
import com.middleware.jms.core.resource.consumer.JmsConsumerResource;
import com.middleware.jms.message.TestingMessage;
import org.apache.commons.pool2.ObjectPool;
import org.apache.log4j.Logger;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

@JmsConsumer(instances = 4, selector = MessageSelector.class)
@DurableConsumerListener
@JmsDestination(name = "queue-durable", destinationType = DestinationType.QUEUE, clazzSuffix = EnvironmentSuffix.class)
public class JmsConsumerQueueDurable extends JmsConsumerResource<TestingMessage> {

    private Logger logger = Logger.getLogger(JmsConsumerQueueDurable.class);
    private AtomicInteger atomicInteger;

    public JmsConsumerQueueDurable(ObjectPool<JmsConnection> connectionPool, JmsSessionParameters jmsSessionParameters, JmsResourceDestination jmsResourceDestination, Class<TestingMessage> clazz) {
        super(connectionPool, jmsSessionParameters, jmsResourceDestination, clazz);
    }

    public void setAtomicInteger(AtomicInteger atomicInteger) {
        this.atomicInteger = atomicInteger;
    }

    public void process(TestingMessage testingMessage, Properties properties) {
        logger.info("Message Received " + testingMessage.getId() + " in consumer " + getId());
        atomicInteger.getAndIncrement();
    }

    public void waitUntilMessageReceived(Integer expectedMessages) throws Exception {
        while (atomicInteger.get() < expectedMessages) {
            Thread.sleep(300);
        }
    }


}
