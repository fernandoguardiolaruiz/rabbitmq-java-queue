package com.middleware.jms.resources.topic;


import com.middleware.jms.annotations.JmsDestination;
import com.middleware.jms.annotations.JmsProducer;
import com.middleware.jms.connection.JmsConnection;
import com.middleware.jms.core.JmsSessionParameters;
import com.middleware.jms.core.destination.type.DestinationType;
import com.middleware.jms.core.resource.producer.JmsProducerResource;
import com.middleware.jms.message.TestingMessage;
import com.middleware.jms.core.JmsResourceDestination;
import org.apache.commons.pool2.ObjectPool;

@JmsProducer(routingKey = {"news.uk", "news.es"})
@JmsDestination(name = "amq.topic", schema = "topic", exchange = "amq.topic", destinationType = DestinationType.TOPIC)
public class JmsProducerNewsWorld extends JmsProducerResource<TestingMessage> {

    public JmsProducerNewsWorld(String routingKey, ObjectPool<JmsConnection> connectionPool, JmsSessionParameters jmsSessionParameters, JmsResourceDestination jmsResourceDestination, Class<TestingMessage> clazz) {
        super(routingKey, connectionPool, jmsSessionParameters, jmsResourceDestination, clazz);
    }
}
