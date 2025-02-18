package com.middleware.jms.core.resource;

import com.middleware.jms.annotations.JmsErrorHandler;
import com.middleware.jms.annotations.JmsListener;
import com.middleware.jms.connection.JmsConnection;
import com.middleware.jms.converter.Converter;
import com.middleware.jms.converter.ConverterException;
import com.middleware.jms.converter.ConverterFactory;
import com.middleware.jms.core.JmsResourceDestination;
import com.middleware.jms.core.JmsSessionParameters;
import com.middleware.jms.core.resource.handler.ErrorHandlerComponent;
import com.middleware.jms.core.resource.handler.JmsHandlerResource;
import com.middleware.jms.core.resource.handler.JmsResourceErrorHandler;
import com.middleware.jms.core.resource.listener.JmsResourceListener;
import org.apache.commons.pool2.ObjectPool;
import org.apache.log4j.Logger;

import javax.jms.JMSException;
import javax.jms.Session;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public abstract class JmsResource<T> {

    protected Logger logger = Logger.getLogger(JmsResource.class);
    protected ObjectPool<JmsConnection> connectionPool;
    protected JmsSessionParameters jmsSessionParameters;
    protected JmsResourceDestination jmsResourceDestination;
    protected Class<T> clazz;
    protected Map<String, Converter> mediaTypeConverter = Collections.synchronizedMap(new HashMap<>());
    protected JmsHandlerResource<?, T> jmsHandlerResource;
    protected Set<JmsResourceErrorHandler<T>> jmsResourceErrorHandlers = new HashSet<>();
    protected Set<JmsResourceListener> jmsResourceListeners = new HashSet<>();
    protected ErrorHandlerComponent<T> errorHandlerComponent = new ErrorHandlerComponent();

    public JmsResource() {

    }

    public JmsResource(ObjectPool<JmsConnection> connectionPool, JmsSessionParameters jmsSessionParameters,
            JmsResourceDestination jmsResourceDestination, Class<T> clazz) {

        this.connectionPool = connectionPool;
        this.jmsSessionParameters = jmsSessionParameters;
        this.jmsResourceDestination = jmsResourceDestination;
        this.clazz = clazz;
    }

    public void setConnectionPool(
            ObjectPool<JmsConnection> connectionPool) {

        this.connectionPool = connectionPool;
    }

    public void setJmsSessionParameters(JmsSessionParameters jmsSessionParameters) {

        this.jmsSessionParameters = jmsSessionParameters;
    }

    public void setJmsResourceDestination(JmsResourceDestination jmsResourceDestination) {

        this.jmsResourceDestination = jmsResourceDestination;
    }

    public void setClazz(Class<T> clazz) {

        this.clazz = clazz;
    }

    public void addJmsResourceListeners(JmsResourceListener jmsResourceListener) {

        jmsResourceListeners.add(jmsResourceListener);
    }

    public void addJmsErrorHandlerResource(JmsResourceErrorHandler<T> jmsResourceErrorHandler) {

        this.jmsResourceErrorHandlers.add(jmsResourceErrorHandler);
    }

    public JmsHandlerResource<?, T> getJmsHandlerResource() {

        return jmsHandlerResource;
    }

    public void setJmsHandlerResource(JmsHandlerResource<?, T> jmsHandlerResource) {

        this.jmsHandlerResource = jmsHandlerResource;
    }

    public JmsResourceDestination getJmsResourceDestination() {

        return jmsResourceDestination;
    }

    public abstract void onMessageRecovery(String bodyMessage, Properties properties) throws Exception;

    protected JmsConnection getConnection() throws JMSException {

        try {
            JmsConnection jmsConnection = null;
            jmsConnection = connectionPool.borrowObject();
            logger.debug(
                    "Get connection " + jmsConnection + " from connection pool " + this.getClass().getSimpleName());
            return jmsConnection;
        } catch (Exception ex) {
            throw new JMSException("Can't get connection from connection pool ");
        }
    }

    protected Session getSession(JmsConnection jmsConnection) throws JMSException {

        try {
            return jmsConnection.getConnection()
                    .createSession(jmsSessionParameters.isTransacted(), jmsSessionParameters.getAcknowledgeMode());
        } catch (Exception ex) {
            throw new JMSException("Can't get session from connection " + jmsConnection);
        }
    }

    protected void handleError(Throwable exception, T t, Properties properties) {

        errorHandlerComponent
                .handleError(this.getClass(), jmsResourceErrorHandlers, this::getPriorityFromErrorHandler, exception, t,
                        properties, getJmsResourceType());
    }

    protected void logPropertiesAndMessage(Properties properties, String message) {

        boolean logRequestEnabled = Boolean.parseBoolean(
                Optional.ofNullable((String) properties.get("LogRequestEnabled")).orElse(Boolean.FALSE.toString()));
        if (logger.isDebugEnabled() || logRequestEnabled) {
            if (logger.isDebugEnabled()) {
                logger.debug(this.getClass().getSimpleName()+" Properties: " + properties + " Message: " + message);
            } else if (logRequestEnabled) {
                logger.error(this.getClass().getSimpleName()+" Properties: " + properties + " Message: " + message);
            }
        }
    }

    protected abstract JmsResourceType getJmsResourceType();

    protected void commit(Session session) throws JMSException {

        if (session.getTransacted()) {
            session.commit();
        }
    }

    protected void rollback(Session session) throws JMSException {

        if (session.getTransacted()) {
            session.rollback();
        }
    }

    protected Converter getConverter(String mediaType) throws ConverterException {

        Converter converter = mediaTypeConverter.get(mediaType);
        if (converter == null) {
            converter = createConverter(mediaType);
        }
        return converter;
    }

    private synchronized Converter createConverter(String mediaType) throws ConverterException {

        Converter converter = mediaTypeConverter.get(mediaType);
        if (converter == null) {
            converter = ConverterFactory.createConverter(mediaType, clazz, null);
            mediaTypeConverter.put(mediaType, converter);
        }
        return converter;
    }

    public String toString() {

        return this.getClass().getSimpleName() + "/" + this.jmsResourceDestination.getDestination().toString();
    }

    protected <R extends JmsResourceListener> int getPriorityFromListener(R jmsResourceListener) {

        return jmsResourceListener.getClass().getAnnotation(JmsListener.class).priority();
    }

    protected <E extends JmsResourceErrorHandler> int getPriorityFromErrorHandler(E jmsResourceErrorHandler) {

        return jmsResourceErrorHandler.getClass().getAnnotation(JmsErrorHandler.class).priority();
    }

    protected void close(Session session, JmsConnection jmsConnection) throws JMSException {

        close(session);
        close(jmsConnection);
    }

    protected void close(Session session) throws JMSException {

        if (session != null) {
            try {
                session.close();
            } catch (Exception ex) {
                logger.error("Error closing JMS session for resource " + this.getClass().getSimpleName(), ex);
            }
        }
    }

    protected void close(JmsConnection jmsConnection) throws JMSException {

        if (jmsConnection != null) {
            try {
                connectionPool.returnObject(jmsConnection);
            } catch (Exception ex) {
                throw new JMSException(ex.getMessage());
            }
        }
    }

    protected void invalidate(JmsConnection jmsConnection) throws JMSException {

        if (jmsConnection != null) {
            try {
                connectionPool.invalidateObject(jmsConnection);
            } catch (Exception ex) {
                throw new JMSException(ex.getMessage());
            }
        }
    }

}
