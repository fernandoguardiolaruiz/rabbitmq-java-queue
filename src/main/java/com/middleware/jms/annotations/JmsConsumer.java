package com.middleware.jms.annotations;


import com.middleware.jms.core.DefaultJmsSelector;
import com.middleware.jms.core.JmsSelector;

import javax.jms.Session;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JmsConsumer {

    boolean transacted() default false;

    int acknoledgement() default Session.AUTO_ACKNOWLEDGE;

    Class<? extends JmsSelector> selector() default DefaultJmsSelector.class;

    int instances() default 1;


}
