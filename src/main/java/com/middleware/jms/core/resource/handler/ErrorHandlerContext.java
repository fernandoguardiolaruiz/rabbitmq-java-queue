package com.middleware.jms.core.resource.handler;

import com.middleware.jms.core.resource.JmsResource;
import com.middleware.jms.core.resource.JmsResourceType;

import java.util.Properties;

public class ErrorHandlerContext<T> {

    private Throwable exception;
    private T t;
    private Properties properties;
    private JmsResourceType jmsResourceType;
    private Class<? extends JmsResource> clazz;

    public Throwable getException() {

        return exception;
    }

    public void setException(Throwable exception) {

        this.exception = exception;
    }

    public T getT() {

        return t;
    }

    public void setT(T t) {

        this.t = t;
    }

    public Properties getProperties() {

        return properties;
    }

    public void setProperties(Properties properties) {

        this.properties = properties;
    }

    public JmsResourceType getJmsResourceType() {

        return jmsResourceType;
    }

    public void setJmsResourceType(JmsResourceType jmsResourceType) {

        this.jmsResourceType = jmsResourceType;
    }

    public Class<? extends JmsResource> getClazz() {

        return clazz;
    }

    public void setClazz(Class<? extends JmsResource> clazz) {

        this.clazz = clazz;
    }
}
