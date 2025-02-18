package com.middleware.jms.resources.queue.transients;


import com.middleware.jms.core.resource.producer.JmsProducerResource;
import com.middleware.jms.annotations.JmsDestination;
import com.middleware.jms.annotations.JmsProducer;
import com.middleware.jms.connection.JmsConnection;
import com.middleware.jms.core.JmsResourceDestination;
import com.middleware.jms.core.JmsSessionParameters;
import com.middleware.jms.core.destination.type.DestinationType;
import com.middleware.jms.message.TestingMessage;
import org.apache.commons.pool2.ObjectPool;

@JmsProducer
@JmsDestination(name = "queue-transient", destinationType = DestinationType.QUEUE, durable = false)
public class JmsProducerQueueTransient extends JmsProducerResource<TestingMessage> {

    public JmsProducerQueueTransient(String routingKey, ObjectPool<JmsConnection> connectionPool, JmsSessionParameters jmsSessionParameters, JmsResourceDestination jmsResourceDestination, Class<TestingMessage> clazz) {
        super(routingKey, connectionPool, jmsSessionParameters, jmsResourceDestination, clazz);
    }
}
