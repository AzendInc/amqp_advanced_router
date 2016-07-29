package com.azendinc.amqp.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface AmqpMessageHandler {
    String type();
    String methodName() default "handle";
}
