package com.middleware.jms;

import com.middleware.jms.annotations.JmsConsumer;
import com.middleware.jms.annotations.JmsDestination;
import com.middleware.jms.annotations.JmsProducer;
import com.middleware.jms.configuration.JmsConnectionConfiguration;
import com.middleware.jms.core.destination.type.DestinationNamer;
import com.middleware.jms.core.destination.type.DestinationTypeFunctionExecutor;
import com.middleware.jms.core.destination.type.DestinationTypeFunctionType;
import com.middleware.jms.core.destination.type.ValidateJmsDestinationParameters;
import com.middleware.jms.core.resource.consumer.creator.ValidTopicID;
import com.middleware.jms.core.JmsResourceFactory;
import com.middleware.jms.core.JmsResources;
import org.apache.log4j.Logger;
import org.apache.qpid.url.AMQBindingURL;
import org.reflections.Reflections;

import javax.jms.JMSException;
import javax.naming.InitialContext;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class JmsFactory {

    private Logger logger = Logger.getLogger(JmsFactory.class);
    private DestinationTypeFunctionExecutor destinationTypeFunctionExecutor;
    private JmsResourceFactory jmsResourceFactory;

    private JmsFactory() {

        System.setProperty("IMMEDIATE_PREFETCH", "true");
        destinationTypeFunctionExecutor = new DestinationTypeFunctionExecutor();
        this.jmsResourceFactory = new JmsResourceFactory();
        jmsResourceFactory.setDestinationTypeFunctionExecutor(destinationTypeFunctionExecutor);
    }

    public static JmsFactory newInstance() {

        return new JmsFactory();
    }

    private Properties discoverConsumerAndProducers(Reflections reflections,
                                                    Class<? extends Annotation> annotation) throws JMSException {

        Properties destinationProperties = new Properties();
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(annotation);
        if (!classes.isEmpty()) {
            for (Class clazz : classes) {
                if (clazz.isAnnotationPresent(JmsDestination.class)) {
                    JmsDestination jmsDestination = (JmsDestination) clazz.getAnnotation(JmsDestination.class);
                    if (annotation.isAssignableFrom(JmsConsumer.class)) {
                        ValidateJmsDestinationParameters validateJmsDestinationParameters = new ValidateJmsDestinationParameters(
                                jmsDestination);
                        ValidTopicID validTopicID = (ValidTopicID) destinationTypeFunctionExecutor
                                .execute(DestinationTypeFunctionType.VALIDATE_JMS_DESTINATION,
                                        jmsDestination.destinationType(), validateJmsDestinationParameters);
                        if (!validTopicID.isValid()) {
                            throw new JMSException("Missing topicID in @JmsDestination for @JmsConsumer DURABLE");
                        }
                        destinationProperties.put(jmsDestination.destinationType().getReference() + "." +
                                DestinationNamer.getDestinationSuffixName(jmsDestination), getBindingUrlForConsumer(jmsDestination));
                    } else if (annotation.isAssignableFrom(JmsProducer.class)) {
                        JmsProducer jmsProducer = (JmsProducer) clazz.getAnnotation(JmsProducer.class);
                        Arrays.stream(jmsProducer.routingKey()).forEach(routingKey -> {
                            try {
                                destinationProperties.put(jmsDestination.destinationType().getReference() + "." +
                                                getDestinationName(jmsDestination, routingKey),
                                        getBindingUrlForProducer(jmsDestination, routingKey));
                            } catch (JMSException ex) {
                                logger.error("Can't create producer destination for " +
                                        getRoutingKey(jmsDestination, routingKey));
                            }
                        });
                    }
                }
            }
        }
        return destinationProperties;
    }

    private String getDestinationName(JmsDestination jmsDestination, String routingKey) {

        String destinationName = DestinationNamer.getDestinationSuffixName(jmsDestination);
        if (!routingKey.isEmpty()) {
            destinationName = destinationName + "_" + routingKey;
        }
        return destinationName;
    }

    private String getRoutingKey(JmsDestination jmsDestination, String routingKey) {

        return routingKey.isEmpty() ? DestinationNamer.getDestinationSuffixName(jmsDestination) : routingKey;
    }

    private AMQBindingURL getBindingUrlForProducer(JmsDestination jmsDestination,
                                                   String routingKey) throws JMSException {

        try {
            return new AMQBindingURL(jmsDestination.schema() + "://" +  getExchangeName(jmsDestination) + "//" +
                    DestinationNamer.getDestinationSuffixName(jmsDestination) + "?routingkey='" +
                    getRoutingKey(jmsDestination, routingKey) + "'&durable='" + jmsDestination.durable() + "'");
        } catch (Exception ex) {
            throw new JMSException(ex.getMessage());
        }
    }

    private AMQBindingURL getBindingUrlForConsumer(JmsDestination jmsDestination) throws JMSException {

        try {
            return new AMQBindingURL(jmsDestination.schema() + "://" + getExchangeName(jmsDestination) + "//" +
                    DestinationNamer.getDestinationSuffixName(jmsDestination) + "?routingkey='" +
                    DestinationNamer.getDestinationSuffixName(jmsDestination) + "'&durable='" + jmsDestination.durable() + "'");
        } catch (Exception ex) {
            throw new JMSException(ex.getMessage());
        }
    }

    private String getExchangeName(JmsDestination jmsDestination) {
        String exchangeName = jmsDestination.exchange();
        if (exchangeName.equals("amq.direct")) {
            return exchangeName;
        }else{
            return DestinationNamer.getExchangeSuffixName(jmsDestination);
        }
    }


    private Properties discoverJMS(Reflections reflections,
                                   JmsConnectionConfiguration jmsConnectionConfiguration) throws JMSException {

        Properties jmsProperties = null;
        jmsProperties = new Properties();
        jmsProperties.put("java.naming.factory.initial", "com.middleware.jms.core.JmsInitialContextFactory");
        jmsProperties.put("connectionfactory.qpidConnectionFactory", jmsConnectionConfiguration.getUrlConnection());
        jmsProperties.putAll(discoverConsumerAndProducers(reflections, JmsProducer.class));
        jmsProperties.putAll(discoverConsumerAndProducers(reflections, JmsConsumer.class));
        return jmsProperties;
    }

    public JmsResources createJmsResources(List<String> packages,
                                           JmsConnectionConfiguration jmsConnectionConfiguration) throws Exception {

        Reflections reflections = new Reflections(packages);
        JmsResources jmsResources = new JmsResources();
        Properties jmsProperties = discoverJMS(reflections, jmsConnectionConfiguration);
        if (jmsProperties != null) {
            InitialContext initialContext = null;
            try {
                initialContext = new InitialContext(jmsProperties);
                jmsResourceFactory.configure(initialContext, jmsConnectionConfiguration);
            } catch (Exception ex) {
                logger.error("Error configuring jms connection", ex);
            }
            for (Class clazz : reflections.getTypesAnnotatedWith(JmsProducer.class)) {
                JmsProducer jmsProducer = (JmsProducer) clazz.getAnnotation(JmsProducer.class);
                jmsResources.addProducers(jmsResourceFactory.createProducers(clazz, jmsProducer.routingKey()));
            }
            for (Class clazz : reflections.getTypesAnnotatedWith(JmsConsumer.class)) {
                jmsResources.addConsumers(jmsResourceFactory.createConsumers(clazz));
            }
            jmsResources.setJmsResourceListeners(
                    jmsResourceFactory.createListeners(reflections, jmsResources.getJmsResources()));
            jmsResources.setJmsResourceErrorHandlers(
                    jmsResourceFactory.createErrorHandlers(reflections, jmsResources.getJmsResources()));
            jmsResourceFactory.createHandlers(reflections, jmsResources.getJmsResources());
        }
        return jmsResources;
    }

}