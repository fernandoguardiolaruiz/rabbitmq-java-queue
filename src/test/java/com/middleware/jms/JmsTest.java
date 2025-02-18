package com.middleware.jms;

import com.middleware.jms.configuration.JmsConnectionConfiguration;
import com.middleware.jms.configuration.JmsConnectionPoolConfiguration;
import com.middleware.jms.resources.queue.durable.JmsConsumerQueueDurable;
import com.middleware.jms.resources.queue.durable.JmsProducerQueueDurable;
import com.middleware.jms.resources.queue.transients.JmsConsumerQueueTransient;
import com.middleware.jms.resources.queue.transients.JmsProducerQueueTransient;
import com.middleware.jms.configuration.JmsConnectionCredentials;
import com.middleware.jms.core.JmsResources;
import com.middleware.jms.message.TestingMessage;
import com.middleware.jms.resources.topic.JmsConsumerES;
import com.middleware.jms.resources.topic.JmsConsumerNews;
import com.middleware.jms.resources.topic.JmsConsumerUK;
import com.middleware.jms.resources.topic.JmsProducerNewsWorld;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jms.JMSException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.fail;

public class JmsTest {

    private Logger logger = Logger.getLogger(JmsTest.class);

    private JmsResources jmsResources;
    private JmsProducerQueueTransient jmsProducerQueueTransient;
    private JmsConsumerQueueTransient jmsConsumerQueueTransient;

    private JmsProducerQueueDurable jmsProducerQueueDurable;
    private JmsConsumerQueueDurable jmsConsumerQueueDurable;


    @Before
    public void init() throws Exception {
        JmsConnectionConfiguration jmsConnectionConfiguration = new JmsConnectionConfiguration();
        jmsConnectionConfiguration.setTcpHost("tcp://localhost:5672");
        JmsConnectionCredentials jmsConnectionCredentials = new JmsConnectionCredentials();
        jmsConnectionCredentials.setUsername("admin");
        jmsConnectionCredentials.setPassword("admin");

        JmsConnectionPoolConfiguration jmsConnectionPoolConfiguration = new JmsConnectionPoolConfiguration();
        jmsConnectionPoolConfiguration.setMinIdle(1);
        jmsConnectionPoolConfiguration.setMaxIdle(5);
        jmsConnectionPoolConfiguration.setMaxTotal(10);
        jmsConnectionConfiguration.setJmsConnectionPoolConfiguration(jmsConnectionPoolConfiguration);
        jmsConnectionConfiguration.setJmsConnectionCredentials(jmsConnectionCredentials);

        JmsFactory jmsFactory = JmsFactory.newInstance();
        jmsResources = jmsFactory.createJmsResources(Arrays.asList("com.middleware.jms.resources"), jmsConnectionConfiguration);
        jmsProducerQueueTransient = jmsResources.getJmsProducer(JmsProducerQueueTransient.class);
        jmsConsumerQueueTransient = jmsResources.getJmsConsumer(JmsConsumerQueueTransient.class);
        jmsProducerQueueDurable = jmsResources.getJmsProducer(JmsProducerQueueDurable.class);
        jmsConsumerQueueDurable = jmsResources.getJmsConsumer(JmsConsumerQueueDurable.class);
    }

    @After
    public void shutdown() throws JMSException {
        jmsResources.close();
    }

    @Test
    public void sendAndReceiveMessageToTestQueueTransient() throws Exception {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        jmsResources.getJmsConsumers(JmsConsumerQueueTransient.class).stream().forEach(c -> c.setAtomicInteger(atomicInteger));
        jmsResources.start(JmsConsumerQueueTransient.class);
        for (int i = 0; i < 4; i++) {
            final int iMessage = i;
            CompletableFuture.runAsync(() -> {
                TestingMessage testMessaage = new TestingMessage();
                try {
                    testMessaage.setMessage("Hola Mundo! (Queue Transient)");
                    testMessaage.setId(iMessage);
                    jmsProducerQueueTransient.send(testMessaage);
                } catch (Exception ex) {
                    logger.error("Error sending message " + testMessaage.getId());
                }
            }).get();
        }
        jmsResources.getJmsConsumers(JmsConsumerQueueTransient.class).stream().forEach(c -> {
            try {
                c.waitUntilMessageReceived(4);
            } catch (Exception ex) {
                fail(ex.getMessage());
            }
        });
    }

    @Test
    public void sendAndReceiveMessageToTestQueueDurable() throws Exception {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        jmsResources.getJmsConsumers(JmsConsumerQueueDurable.class).stream().forEach(c -> c.setAtomicInteger(atomicInteger));
        jmsResources.start(JmsConsumerQueueDurable.class);
        for (int i = 0; i < 3; i++) {
            final int iMessage = i;
            CompletableFuture.runAsync(() -> {
                TestingMessage testMessaage = new TestingMessage();
                try {
                    testMessaage.setMessage("Hola Mundo! (Queue Durable)");
                    testMessaage.setId(iMessage);
                    jmsProducerQueueDurable.send(testMessaage);
                } catch (Exception ex) {
                    logger.error("Error sending message " + testMessaage.getId());
                }
            }).get();
        }
        jmsResources.getJmsConsumers(JmsConsumerQueueDurable.class).stream().forEach(c -> {
            try {
                c.waitUntilMessageReceived(3);
            } catch (Exception ex) {
                fail(ex.getMessage());
            }
        });
    }


    @Test
    public void sendAndReceiveMessageToTestTopic() throws Exception {
        TestingMessage news = new TestingMessage();
        news.setMessage("News");

        jmsResources.getJmsConsumer(JmsConsumerES.class).start(false);
        jmsResources.getJmsConsumer(JmsConsumerUK.class).start(false);
        jmsResources.getJmsConsumer(JmsConsumerNews.class).start(false);
        logger.info("Sending message " + news.getMessage());
        jmsResources.getJmsProducers(JmsProducerNewsWorld.class).stream().forEach(p -> {
            try {
                p.send(news);
            } catch (Exception ex) {
                logger.error(ex);
            }
        });

        jmsResources.getJmsConsumer(JmsConsumerES.class).waitUntilMessageReceived(1);
        jmsResources.getJmsConsumer(JmsConsumerUK.class).waitUntilMessageReceived(1);
        jmsResources.getJmsConsumer(JmsConsumerNews.class).waitUntilMessageReceived(2);
    }


}
