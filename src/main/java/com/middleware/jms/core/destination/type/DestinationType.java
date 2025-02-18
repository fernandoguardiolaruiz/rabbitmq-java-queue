package com.middleware.jms.core.destination.type;

import com.middleware.jms.annotations.JmsTopic;
import com.middleware.jms.annotations.JmsQueue;

import java.lang.annotation.Annotation;

public enum DestinationType {

    QUEUE("queue", JmsQueue.class),
    TOPIC("topic", JmsTopic.class);

    private String reference;
    private Class<? extends Annotation> methodAnnotation;

    DestinationType(String reference, Class<? extends Annotation> methodAnnotation) {
        this.reference = reference;
        this.methodAnnotation = methodAnnotation;

    }
    public Class<? extends Annotation> getMethodAnnotation() {
        return methodAnnotation;
    }

    public String getReference() {
        return reference;
    }

}
