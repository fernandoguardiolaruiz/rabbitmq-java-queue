package com.middleware.jms.resources.queue.transients;

import com.middleware.jms.core.resource.consumer.JmsConsumerResource;
import com.middleware.jms.annotations.JmsConsumer;
import com.middleware.jms.annotations.JmsDestination;
import com.middleware.jms.connection.JmsConnection;
import com.middleware.jms.core.JmsResourceDestination;
import com.middleware.jms.core.JmsSessionParameters;
import com.middleware.jms.core.destination.type.DestinationType;
import com.middleware.jms.message.TestingMessage;
import com.middleware.jms.resources.handler.ConsumerHandler;
import org.apache.commons.pool2.ObjectPool;
import org.apache.log4j.Logger;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

@JmsConsumer(instances = 2)
@ConsumerHandler
@JmsDestination(name = "queue-transient", destinationType = DestinationType.QUEUE, durable = false)
public class JmsConsumerQueueTransient extends JmsConsumerResource<TestingMessage> {

    private Logger logger = Logger.getLogger(JmsConsumerQueueTransient.class);
    private AtomicInteger atomicInteger;

    public JmsConsumerQueueTransient(ObjectPool<JmsConnection> connectionPool, JmsSessionParameters jmsSessionParameters, JmsResourceDestination jmsResourceDestination, Class<TestingMessage> clazz) {
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
