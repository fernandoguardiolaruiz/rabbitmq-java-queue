package com.middleware.jms.annotations;

import com.middleware.jms.core.destination.type.DestinationSuffix;
import com.middleware.jms.core.destination.type.DestinationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JmsDestination {

    String name() default "";

    Class<? extends DestinationSuffix> clazzSuffix() default DefaultDestinationSuffix.class;

    String exchange() default "amq.direct";

    String schema() default "direct";

    String id() default "";

    DestinationType destinationType() default DestinationType.QUEUE;

    boolean durable() default true;

    class DefaultDestinationSuffix implements DestinationSuffix {

        @Override
        public String version() {

            return null;
        }
    }

}
