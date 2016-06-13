package com.azendinc.billassist.injectables.messaging;

import com.azendinc.billassist.injectables.messaging.annotations.AmqpMessageHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class takes the name of the message type and returns a
 * class marked with @AmqpMessageHandler for that type. If none
 * is found an exception is thrown. If there is more than on
 * handler they will be called in a non-guaranteed order.
 */
@Component
public class AmqpMessageHandlerFactory implements ApplicationContextAware, InitializingBean {
    private ApplicationContext applicationContext;

    private Map<String, List<Object>> handlersMap;

    /**
     * Default Constructor
     */
    public AmqpMessageHandlerFactory() {
        handlersMap = new HashMap<>();
    }

    /**
     * Factory Method for getting Handlers from message type
     *
     * @param messageType the message type ApiName.MessageName
     * @return List of message handlers or null if no message handlers
     *         exist for the specified type
     */
    public List<Object> getHandlers(String messageType) {
        return handlersMap.get(messageType);
    }

    /**
     * This method is called in the startup process for the system, here we use teh context
     * to get the classes with the annotation which can take a bit longer
     * than we want to on a request.ddd
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        final Map<String, Object> allHandlers = applicationContext.getBeansWithAnnotation(AmqpMessageHandler.class);
        // here we get all the handlers and group them by the messageType of the handler
        handlersMap = allHandlers.values()
            .stream()
            .collect(Collectors.groupingBy(
                o -> o.getClass().getAnnotation(AmqpMessageHandler.class).type().toString()));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
