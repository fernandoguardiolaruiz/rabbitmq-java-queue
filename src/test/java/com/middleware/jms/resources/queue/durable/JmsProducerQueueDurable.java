package com.middleware.jms.resources.queue.durable;


import com.middleware.jms.annotations.JmsDestination;
import com.middleware.jms.annotations.JmsProducer;
import com.middleware.jms.connection.JmsConnection;
import com.middleware.jms.core.JmsResourceDestination;
import com.middleware.jms.core.JmsSessionParameters;
import com.middleware.jms.core.destination.type.DestinationType;
import com.middleware.jms.core.resource.producer.JmsProducerResource;
import com.middleware.jms.message.TestingMessage;
import com.middleware.jms.resources.handler.ProducerHandler;
import org.apache.commons.pool2.ObjectPool;

@JmsProducer
@ProducerHandler
@JmsDestination(name = "queue-durable", destinationType = DestinationType.QUEUE, clazzSuffix = EnvironmentSuffix.class)
public class JmsProducerQueueDurable extends JmsProducerResource<TestingMessage> {

    public JmsProducerQueueDurable(String routingKey, ObjectPool<JmsConnection> connectionPool, JmsSessionParameters jmsSessionParameters, JmsResourceDestination jmsResourceDestination, Class<TestingMessage> clazz) {
        super(routingKey, connectionPool, jmsSessionParameters, jmsResourceDestination, clazz);
    }
}
