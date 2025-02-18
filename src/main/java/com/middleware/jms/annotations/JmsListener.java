package com.middleware.jms.annotations;

import com.middleware.jms.annotations.listener.JmsAll;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JmsListener {

    Class<? extends Annotation> value() default JmsAll.class;

    int priority() default 0;

}
