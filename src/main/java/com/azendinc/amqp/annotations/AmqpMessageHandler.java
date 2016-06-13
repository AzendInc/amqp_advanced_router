package com.azendinc.billassist.injectables.messaging.annotations;

import com.azendinc.billassist.constants.MessageTypes;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface AmqpMessageHandler {
    MessageTypes type();
    String methodName() default "handle";
}
