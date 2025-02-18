package com.middleware.jms.annotations;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JmsHandler {

    Class<? extends Annotation> value();


}
