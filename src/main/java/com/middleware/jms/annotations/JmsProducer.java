package com.middleware.jms.annotations;

import javax.jms.Session;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JmsProducer {

    boolean transacted() default false;

    int acknoledgement() default Session.AUTO_ACKNOWLEDGE;

    String[] routingKey() default {""};


}
