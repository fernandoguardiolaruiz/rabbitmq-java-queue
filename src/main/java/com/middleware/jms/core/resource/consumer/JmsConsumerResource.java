package com.middleware.jms.core.resource.consumer;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.middleware.jms.core.JmsAcknowledgeListener;
import com.middleware.jms.core.resource.JmsResource;
import com.middleware.jms.core.resource.JmsResourceType;
import com.middleware.jms.core.resource.consumer.creator.MessageConsumerFactory;
import com.middleware.jms.annotations.JmsConsumer;
import com.middleware.jms.connection.JmsConnection;
import com.middleware.jms.converter.Converter;
import com.middleware.jms.core.JmsResourceDestination;
import com.middleware.jms.core.JmsSelector;
import com.middleware.jms.core.JmsSessionParameters;
import com.middleware.jms.core.destination.type.DestinationTypeFunctionExecutor;
import com.middleware.jms.core.resource.handler.HandlerParameters;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.log4j.Logger;
import org.apache.qpid.client.message.JMSBytesMessage;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.ws.rs.core.MediaType;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Properties;

public abstract class JmsConsumerResource<T> extends JmsResource<T> implements MessageListener {

    private Logger logger = Logger.getLogger(this.getClass());
    private MessageConsumer messageConsumer;
    private JmsAcknowledgeListener jmsAcknowledgeListener;
    private MessageConsumerFactory messageConsumerFactory;
    private Session session;
    private JmsConnection jmsConnection;
    private boolean started = false;
    private Integer id;

    public JmsConsumerResource() {

    }

    public JmsConsumerResource(ObjectPool<JmsConnection> connectionPool, JmsSessionParameters jmsSessionParameters,
            JmsResourceDestination jmsResourceDestination, Class<T> clazz) {

        super(connectionPool, jmsSessionParameters, jmsResourceDestination, clazz);
    }

    public void setDurabilityFunctionExecutor(DestinationTypeFunctionExecutor destinationTypeFunctionExecutor) {

        this.messageConsumerFactory = MessageConsumerFactory.getInstance(destinationTypeFunctionExecutor);
    }

    public Integer getId() {

        return id;
    }

    public void setId(Integer id) {

        this.id = id;
    }

    public void restart(boolean force) {

        stop(force);
        start(force);
    }

    public void start(boolean force) {

        try {
            if (!started || force) {
                jmsConnection = getConnection();
                ExceptionConnectionListener connectionListener = new ExceptionConnectionListener(this);
                jmsConnection.getConnection().setExceptionListener(connectionListener);
                session = getSession(jmsConnection);
                messageConsumer = messageConsumerFactory
                        .createMesssageConsumer(session, jmsResourceDestination, createMessageSelector());
                logger.info("Started jms: " + this.toString());
                messageConsumer.setMessageListener(this);
                started = true;
            }
        } catch (Exception e) {
            logger.error("Error creating a consumer ", e);
        }
    }

    public boolean isStarted() {

        return Optional.ofNullable(jmsConnection).map(JmsConnection::isStarted).orElse(Boolean.FALSE);
    }

    private String createMessageSelector() throws Exception {

        JmsConsumer jmsConsumer = this.getClass().getAnnotation(JmsConsumer.class);
        JmsSelector jmsSelector = jmsConsumer.selector().newInstance();
        StringBuffer messageSelector = new StringBuffer();
        if (jmsSelector.properties() != null) {
            Properties properties = jmsSelector.properties();
            for (String property : properties.stringPropertyNames()) {
                messageSelector.append(property).append(" = ").append("'").append(properties.getProperty(property))
                        .append("'").append(" and ");
            }
            messageSelector.setLength(messageSelector.length() - 5);
            logger.debug("Created message selector " + messageSelector.toString());
        }
        return messageSelector.toString();
    }

    public void stop(boolean force) {

        try {
            if (started || force) {
                logger.info("Stopping consumer " + this.toString());
                messageConsumer.close();
                close(session);
                invalidate(jmsConnection);
                started = false;
            }
        } catch (RuntimeException rex) {
            logger.error("Error stopping consumer " + this.toString(), rex);
        } catch (Exception ex) {
            logger.error("Error stopping consumer " + this.toString(), ex);
        }
    }

    public void setJmsAcknowledgeListener(JmsAcknowledgeListener jmsAcknowledgeListener) {

        this.jmsAcknowledgeListener = jmsAcknowledgeListener;
    }

    public abstract void process(T t, Properties properties) throws Exception;

    public JmsResourceType getJmsResourceType() {

        return JmsResourceType.CONSUMER;
    }

    public void onMessage(Message message) {

        Properties properties = null;
        String bodyMessage = null;
        try {
            properties = readPropertiesFromMessage(message);
            bodyMessage = getBodyMessage(message);
            String contentType = StringUtils
                    .defaultString(message.getStringProperty("Content-Type"), MediaType.APPLICATION_JSON);
            logPropertiesAndMessage(properties, bodyMessage);
            T t = getT(contentType, bodyMessage);
            if (jmsAcknowledgeListener != null) {
                jmsAcknowledgeListener.acknowledge(message);
            }
            HandlerParameters handlerParameters = new HandlerParameters();
            handlerParameters.setMessage(t);
            handlerParameters.setProperties(properties);
            handlerParameters.setHandlerError(true);
            handleMessage(handlerParameters);
        } catch (Exception e) {
            logger.error("Error consuming message ", e);
            handleError(e, (T) bodyMessage, properties);
        }
    }


    public void onMessageRecovery(String bodyMessage, Properties properties) throws Exception {

        String contentType = StringUtils
                .defaultString(properties.getProperty("Content-Type"), MediaType.APPLICATION_JSON);
        HandlerParameters handlerParameters = new HandlerParameters();
        handlerParameters.setProperties(properties);
        handlerParameters.setMessage(getT(contentType, bodyMessage));
        handlerParameters.setHandlerError(false);
        handleMessage(handlerParameters);
    }

    private T getT(String contentType, String bodyMessage) throws Exception {

        Converter converter = getConverter(contentType);
        T t = (T) converter.toObject(bodyMessage, new JavaTimeModule(), new Jdk8Module());
        return t;
    }

    private String getBodyMessage(Message message) throws Exception {

        String plainMessage = null;
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            plainMessage = textMessage.getText();
        } else if (message instanceof JMSBytesMessage) {
            JMSBytesMessage jmsBytesMessage = (JMSBytesMessage) message;
            byte b[] = new byte[(int) jmsBytesMessage.getBodyLength()];
            jmsBytesMessage.readBytes(b);
            plainMessage = new String(b);
        } else {
            throw new Exception("Invalid message class " + message.getClass().getName());
        }
        return plainMessage;
    }

    private void handleMessage(HandlerParameters handlerParameters) {

        if (jmsHandlerResource == null) {
            processWithListeners(handlerParameters);
        } else {
            jmsHandlerResource.handle(this::processWithListeners, handlerParameters);
        }
    }

    public void processWithListeners(HandlerParameters handlerParameters) {

        CollectionUtils.emptyIfNull(jmsResourceListeners).stream()
                .sorted(Comparator.comparingInt(rl -> getPriorityFromListener(rl)))
                .forEach(l -> l.onBeforeProcessingMessage(handlerParameters.getProperties()));
        try {
            process((T) handlerParameters.getMessage(), handlerParameters.getProperties());
        } catch (Throwable exception) {
            logger.error("Error processing message " + this.getClass().getSimpleName(), exception);
            if (handlerParameters.isHandlerError()) {
                handleError(exception, (T) handlerParameters.getMessage(), handlerParameters.getProperties());
            } else {
                throw new RuntimeException(exception);
            }
        }
    }

    private Properties readPropertiesFromMessage(Message message) throws Exception {

        Properties properties = new Properties();
        Enumeration e = message.getPropertyNames();
        while (e.hasMoreElements()) {
            String property = e.nextElement().toString();
            properties.setProperty(property, message.getStringProperty(property));
        }
        return properties;
    }

}
