package com.middleware.jms.core;

import com.middleware.jms.annotations.listener.JmsAll;
import com.middleware.jms.annotations.listener.JmsAllConsumers;
import com.middleware.jms.annotations.listener.JmsAllProducers;
import com.middleware.jms.configuration.JmsConnectionConfiguration;
import com.middleware.jms.connection.JmsConnection;
import com.middleware.jms.connection.JmsConnectionManager;
import com.middleware.jms.connection.JmsConnectionPoolFactory;
import com.middleware.jms.core.destination.type.DestinationNamer;
import com.middleware.jms.core.destination.type.DestinationTypeFunctionExecutor;
import com.middleware.jms.core.resource.JmsResource;
import com.middleware.jms.core.resource.consumer.JmsConsumerResource;
import com.middleware.jms.core.resource.handler.JmsHandlerResource;
import com.middleware.jms.core.resource.handler.JmsResourceErrorHandler;
import com.middleware.jms.core.resource.listener.JmsResourceListener;
import com.middleware.jms.core.resource.producer.JmsProducerResource;
import com.middleware.jms.annotations.JmsConsumer;
import com.middleware.jms.annotations.JmsDestination;
import com.middleware.jms.annotations.JmsErrorHandler;
import com.middleware.jms.annotations.JmsHandler;
import com.middleware.jms.annotations.JmsListener;
import com.middleware.jms.annotations.JmsProducer;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component(value = "JmsResourceFactory")
public class JmsResourceFactory implements ApplicationContextAware {

    private InitialContext initialContext;
    private JmsConnectionManager jmsConnectionManager;
    private DestinationTypeFunctionExecutor destinationTypeFunctionExecutor;
    private Logger logger = Logger.getLogger(JmsResourceFactory.class);
    private GenericObjectPoolConfig genericObjectPoolConfig;
    private GenericObjectPool<JmsConnection> connectionPool;
    private static ApplicationContext applicationContext;

    public JmsResourceFactory() {

    }

    public void setDestinationTypeFunctionExecutor(
            DestinationTypeFunctionExecutor destinationTypeFunctionExecutor) {

        this.destinationTypeFunctionExecutor = destinationTypeFunctionExecutor;
    }

    public void setInitialContext(InitialContext initialContext) {

        this.initialContext = initialContext;
    }

    public void configure(InitialContext initialContext, JmsConnectionConfiguration jmsConnectionConfiguration) {

        setInitialContext(initialContext);
        try {
            ConnectionFactory connectionFactory = (ConnectionFactory) initialContext.lookup("qpidConnectionFactory");
            jmsConnectionManager = new JmsConnectionManager(connectionFactory, jmsConnectionConfiguration);
            genericObjectPoolConfig = new GenericObjectPoolConfig();
            genericObjectPoolConfig
                    .setMinIdle(jmsConnectionConfiguration.getJmsConnectionPoolConfiguration().getMinIdle());
            genericObjectPoolConfig
                    .setMaxIdle(jmsConnectionConfiguration.getJmsConnectionPoolConfiguration().getMaxIdle());
            genericObjectPoolConfig
                    .setMaxTotal(jmsConnectionConfiguration.getJmsConnectionPoolConfiguration().getMaxTotal());
            JmsConnectionPoolFactory jmsConnectionPoolFactory = JmsConnectionPoolFactory
                    .getInstance(jmsConnectionManager);
            connectionPool = new GenericObjectPool<JmsConnection>(jmsConnectionPoolFactory, genericObjectPoolConfig);
        } catch (Exception ex) {
            logger.error("Error configuring resource factory ", ex);
        }
    }

    public <L extends JmsResourceListener, R extends JmsResource> Set<L> createListeners(Reflections reflections,
                                                                                         Collection<R> resources) {

        Set<Class<L>> clazzListeners = (Set) reflections.getTypesAnnotatedWith(JmsListener.class);
        return clazzListeners.stream().map(clazzListener -> {
            try {
                JmsListener jmsListener = clazzListener.getAnnotation(JmsListener.class);
                L jmsRsourceListener = getInstance(clazzListener);
                resources.stream().filter(r -> jmsListener.value().isAssignableFrom(JmsAll.class) ||
                        mathClazzAnnotationCorresponding(r, jmsListener.value())
                        || r.getClass().isAnnotationPresent(jmsListener.value()))
                        .forEach(r -> r.addJmsResourceListeners(jmsRsourceListener));
                return jmsRsourceListener;
            } catch (Exception ex) {
                logger.error("Can not instatiate listener for class " + clazzListener.getSimpleName());
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public <E extends JmsResourceErrorHandler, R extends JmsResource> Set<E> createErrorHandlers(
            Reflections reflections, Collection<R> resources) {

        Set<Class<E>> clazzErrorHandlers = (Set) reflections.getTypesAnnotatedWith(JmsErrorHandler.class);
        return clazzErrorHandlers.stream().map(clazzErrorHandler -> {
            try {
                JmsErrorHandler jmsErrorHandler = clazzErrorHandler.getAnnotation(JmsErrorHandler.class);
                E jmsErrorHandlerResource = getInstance(clazzErrorHandler);
                resources.stream().filter(r -> jmsErrorHandler.value().isAssignableFrom(JmsAll.class) ||
                        mathClazzAnnotationCorresponding(r, jmsErrorHandler.value())
                        || r.getClass().isAnnotationPresent(jmsErrorHandler.value()))
                        .forEach(r -> r.addJmsErrorHandlerResource(jmsErrorHandlerResource));
                return jmsErrorHandlerResource;
            } catch (Exception ex) {
                logger.error("Can not instatiate listener for class " + clazzErrorHandler.getSimpleName());
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private <R extends JmsResource> boolean mathClazzAnnotationCorresponding(R resorce,
                                                                             Class<? extends Annotation> annotation) {

        boolean match = false;
        if (JmsConsumerResource.class.isAssignableFrom(resorce.getClass())) {
            match = annotation.isAssignableFrom(JmsAllConsumers.class);
        }
        if (JmsProducerResource.class.isAssignableFrom(resorce.getClass())) {
            match = annotation.isAssignableFrom(JmsAllProducers.class);
        }
        return match;
    }

    public <H extends JmsHandlerResource, R extends JmsResource> void createHandlers(Reflections reflections,
                                                                                     Collection<R> resources) {

        Set<Class<H>> clazzHandlers = (Set) reflections.getTypesAnnotatedWith(JmsHandler.class);
        clazzHandlers.stream().forEach(clazzHandler -> {
            JmsHandler jmsHandler = clazzHandler.getAnnotation(JmsHandler.class);
            resources.stream().filter(r -> r.getClass().isAnnotationPresent(jmsHandler.value()))
                    .forEach(r -> {
                        try {
                            if (r.getJmsHandlerResource() == null) {
                                r.setJmsHandlerResource(getInstance(clazzHandler));
                            } else {
                                logger.error("Only allowed one handler for a consumer");
                            }
                        } catch (Exception ex) {
                            logger.error("Error configuring handler " + clazzHandler.getSimpleName() + " in consumer " +
                                    r.getClass());
                        }
                    });
        });
    }

    public <T extends JmsResource> List<T> createProducers(Class<T> clazz, String[] routingKeys) {

        return (List) Arrays.stream(routingKeys).map(routingKey -> {
            JmsProducer jmsProducer = clazz.getAnnotation(JmsProducer.class);
            JmsResourceDestination jmsResourceDestination = null;
            try {
                jmsResourceDestination = dicoverJmsResourceDestination(clazz, JmsProducer.class, routingKey);
            } catch (Exception ex) {
                logger.error("Error discovering detination jms for: " + clazz.getSimpleName(), ex);
            }
            JmsSessionParameters jmsSessionParameters = new JmsSessionParameters(jmsProducer.transacted(),
                    jmsProducer.acknoledgement());
            JmsProducerResource jmsProducerResource = null;
            try {
                Class<?> genericType = (Class) ((ParameterizedType) getJmsResourceClazz(clazz).getGenericSuperclass())
                        .getActualTypeArguments()[0];
                if (applicationContext == null) {
                    jmsProducerResource = (JmsProducerResource) clazz
                            .getConstructor(String.class, ObjectPool.class, JmsSessionParameters.class,
                                    JmsResourceDestination.class, Class.class)
                            .newInstance(getRoutingKey(jmsResourceDestination.getDestinationName(), routingKey),
                                    connectionPool, jmsSessionParameters, jmsResourceDestination, genericType);
                } else {
                    jmsProducerResource = (JmsProducerResource) applicationContext.getBean(clazz);
                    jmsProducerResource.setConnectionPool(connectionPool);
                    jmsProducerResource.setJmsSessionParameters(jmsSessionParameters);
                    jmsProducerResource.setJmsResourceDestination(jmsResourceDestination);
                    jmsProducerResource.setClazz(genericType);
                    jmsProducerResource
                            .setRoutingKey(getRoutingKey(jmsResourceDestination.getDestinationName(), routingKey));
                }
                return jmsProducerResource;
            } catch (Exception ex) {
                logger.error(
                        "Can't create producer for class " + clazz.getSimpleName() + " and routingKey " + routingKey);
            }
            return jmsProducerResource;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Class<?> getJmsResourceClazz(Class clazz) throws Exception {

        if (clazz.getSuperclass() != null) {
            if (clazz.getSuperclass().isAssignableFrom(JmsProducerResource.class) ||
                    clazz.getSuperclass().isAssignableFrom(JmsConsumerResource.class)) {
                return clazz;
            } else {
                return getJmsResourceClazz(clazz.getSuperclass());
            }
        } else {
            throw new Exception("No found valid clazz " + clazz.getSimpleName());
        }
    }

    private String getRoutingKey(String jmsDestinationName, String routingKey) {

        return routingKey.isEmpty() ? jmsDestinationName : routingKey;
    }

    public <T extends JmsResource> Collection<JmsConsumerResource> createConsumers(Class<T> clazz) throws Exception {

        JmsConsumer jmsConsumer = clazz.getAnnotation(JmsConsumer.class);
        return IntStream.range(0, jmsConsumer.instances()).mapToObj(id -> {
            JmsResourceDestination jmsResourceDestination = null;
            JmsConsumerResource jmsConsumerResource = null;
            try {
                jmsResourceDestination = dicoverJmsResourceDestination(clazz, JmsConsumer.class, null);
            } catch (Exception ex) {
                logger.error("Error discovering detination jms for: " + clazz.getSimpleName(), ex);
            }
            try {
                JmsSessionParameters jmsSessionParameters = new JmsSessionParameters(jmsConsumer.transacted(),
                        jmsConsumer.acknoledgement());
                Class<?> genericType = (Class) ((ParameterizedType) getJmsResourceClazz(clazz).getGenericSuperclass())
                        .getActualTypeArguments()[0];

                if (applicationContext == null) {
                    jmsConsumerResource = (JmsConsumerResource) clazz
                            .getConstructor(ObjectPool.class, JmsSessionParameters.class, JmsResourceDestination.class,
                                    Class.class)
                            .newInstance(connectionPool, jmsSessionParameters, jmsResourceDestination, genericType);
                } else {
                    jmsConsumerResource = (JmsConsumerResource) applicationContext.getBean(clazz);
                    jmsConsumerResource.setConnectionPool(connectionPool);
                    jmsConsumerResource.setJmsSessionParameters(jmsSessionParameters);
                    jmsConsumerResource.setJmsResourceDestination(jmsResourceDestination);
                    jmsConsumerResource.setClazz(genericType);
                }

                jmsConsumerResource.setDurabilityFunctionExecutor(destinationTypeFunctionExecutor);
                jmsConsumerResource.setId(id);
            } catch (Exception ex) {
                logger.error("Error creating consumer for class " + clazz.getSimpleName());
            }
            return jmsConsumerResource;
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private <T extends JmsResource> JmsResourceDestination dicoverJmsResourceDestination(Class<T> clazzJMS,
                                                                                         Class<? extends Annotation> annotationClazz,
                                                                                         String routingKey) throws Exception {

        Destination destination = null;
        JmsDestination jmsDestination = null;
        if (clazzJMS.isAnnotationPresent(JmsDestination.class)) {
            jmsDestination = clazzJMS.getAnnotation(JmsDestination.class);
            if (annotationClazz.isAssignableFrom(JmsConsumer.class)) {
                destination = getDestinationQueue(jmsDestination);
            }
            if (annotationClazz.isAssignableFrom(JmsProducer.class)) {
                destination = routingKey.isEmpty() ? getDestinationQueue(jmsDestination) : getDestinationTopic(
                        DestinationNamer.getDestinationSuffixName(jmsDestination), routingKey);
            }
        } else {
            throw new JMSException("Missing @JmsDestination in " + clazzJMS.getSimpleName());
        }
        return new JmsResourceDestination(destination, jmsDestination);
    }

    private <T extends JmsResource> Destination getDestinationQueue(
            JmsDestination jmsDestination) throws NamingException {

        return (Destination) initialContext.lookup(DestinationNamer.getDestinationSuffixName(jmsDestination));
    }

    private <T extends JmsResource> Destination getDestinationTopic(String topicName,String routingkey) throws NamingException {

        return (Destination) initialContext.lookup(topicName+"_" + routingkey);
    }

    private <T> T getInstance(Class<T> clazz) {

        return Optional.ofNullable(applicationContext).map(ctx -> {
            return ctx.getBean(clazz);
        }).orElseGet(() -> {
            T t = null;
            try {
                t = clazz.newInstance();
            } catch (Exception ex) {
                logger.error("Can't get instance for " + clazz);
            }
            return t;
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext myApplicationContext) throws BeansException {

        applicationContext = myApplicationContext;
    }
}