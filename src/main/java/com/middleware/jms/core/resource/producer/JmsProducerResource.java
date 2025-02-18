package com.middleware.jms.core.resource.producer;

import com.middleware.jms.core.resource.JmsResource;
import com.middleware.jms.core.resource.JmsResourceType;
import com.middleware.jms.core.resource.handler.HandlerParameters;
import com.middleware.jms.connection.JmsConnection;
import com.middleware.jms.converter.Converter;
import com.middleware.jms.core.JmsResourceDestination;
import com.middleware.jms.core.JmsSessionParameters;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.log4j.Logger;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.ws.rs.core.MediaType;
import java.util.Comparator;
import java.util.Optional;
import java.util.Properties;

public abstract class JmsProducerResource<T> extends JmsResource<T> {

    private Logger logger = Logger.getLogger(this.getClass());
    private String routingKey;

    public JmsProducerResource() {

    }

    public JmsProducerResource(String routingKey, ObjectPool<JmsConnection> connectionPool,
                               JmsSessionParameters jmsSessionParameters, JmsResourceDestination jmsResourceDestination,
                               Class<T> clazz) {

        super(connectionPool, jmsSessionParameters, jmsResourceDestination, clazz);
        this.routingKey = routingKey;
    }

    public String getRoutingKey() {

        return routingKey;
    }

    public void setRoutingKey(String routingKey) {

        this.routingKey = routingKey;
    }

    protected MessageProducer createProducer(Session session) throws JMSException {

        return session.createProducer(jmsResourceDestination.getDestination());
    }

    public void send(String mediaType, T... ts) throws Exception {

        send(mediaType, new Properties(), ts);
    }

    public void send(T... ts) throws Exception {

        send(MediaType.APPLICATION_JSON, new Properties(), ts);
    }

    public void send(Properties properties, T... ts) throws Exception {

        send(MediaType.APPLICATION_JSON, properties, ts);
    }

    public void send(String mediaType, Properties properties, T... ts) throws Exception {

        Converter converter = getConverter(mediaType);
        JmsConnection jmsConnection = getConnection();
        Session session = getSession(jmsConnection);
        MessageProducer messageProducer = createProducer(session);
        try {
            for (T t : ts) {
                sendTextMessage(session, converter, t, mediaType, messageProducer, properties, true);
            }
            commit(session);
        } catch (Exception e) {
            logger.error("Error sending message ", e);
            rollback(session);
        } finally {
            messageProducer.close();
            close(session, jmsConnection);
        }
    }

    public void onMessageRecovery(String bodyMessage, Properties properties) throws Exception {

        String mediaType = Optional.ofNullable(properties.getProperty("Content-Type"))
                .orElse(MediaType.APPLICATION_JSON);
        Converter converter = getConverter(mediaType);
        JmsConnection jmsConnection = getConnection();
        Session session = getSession(jmsConnection);
        MessageProducer messageProducer = createProducer(session);
        sendTextMessage(session, converter, (T) converter.toObject(bodyMessage), mediaType, messageProducer,
                properties, false);
    }

    private void sendTextMessage(Session session, Converter converter, T t, String mediaType,
                                 MessageProducer messageProducer, Properties properties, boolean handleError) {

        String bodyMessage = null;
        try {
            TextMessage textMessage = session.createTextMessage();
            bodyMessage = converter.toString(t);
            textMessage.setText(bodyMessage);
            logger.debug("Started jms: " + this.toString());
            textMessage.setStringProperty("Content-Type", mediaType);
            ProcessMessageParameters processMessageParameters = new ProcessMessageParameters(messageProducer,
                    textMessage, properties, t, handleError);
            if (jmsHandlerResource == null) {
                processMessage(processMessageParameters);
            } else {
                jmsHandlerResource.handle(this::processMessage, processMessageParameters);
            }
        } catch (Exception ex) {
            handleError(ex, (T) bodyMessage, properties);
        }
    }

    private void processMessage(ProcessMessageParameters processMessageParameters) {

        Properties properties = processMessageParameters.getProperties();
        TextMessage textMessage = processMessageParameters.getTextMessage();
        MessageProducer messageProducer = processMessageParameters.getMessageProducer();
        try {
            CollectionUtils.emptyIfNull(jmsResourceListeners).stream()
                    .sorted(Comparator.comparingInt(rl -> getPriorityFromListener(rl)))
                    .forEach(l -> l.onBeforeProcessingMessage(properties));
            for (String key : processMessageParameters.properties.stringPropertyNames()) {
                textMessage.setStringProperty(key, processMessageParameters.getProperties().getProperty(key));
            }
            logPropertiesAndMessage(properties, processMessageParameters.getTextMessage().getText());
            messageProducer.send(textMessage);
        } catch (Exception exception) {
            if (processMessageParameters.isHandlerError()) {
                logger.error("Error processing message " + this.getClass().getSimpleName(), exception);
                handleError(exception, processMessageParameters.getT(), properties);
            } else {
                throw new RuntimeException(exception);
            }
        }
    }

    public JmsResourceType getJmsResourceType() {
        return JmsResourceType.PRODUCER;
    }

    class ProcessMessageParameters extends HandlerParameters {

        private MessageProducer messageProducer;
        private TextMessage textMessage;
        private Properties properties;
        private T t;

        ProcessMessageParameters(MessageProducer messageProducer, TextMessage textMessage, Properties properties, T t,
                                 boolean handleError) {

            this.messageProducer = messageProducer;
            this.textMessage = textMessage;
            this.properties = properties;
            this.t = t;
            this.handlerError = handleError;
        }

        public MessageProducer getMessageProducer() {

            return messageProducer;
        }

        public TextMessage getTextMessage() {

            return textMessage;
        }

        public Properties getProperties() {

            return properties;
        }

        public T getT() {

            return t;
        }
    }

}
