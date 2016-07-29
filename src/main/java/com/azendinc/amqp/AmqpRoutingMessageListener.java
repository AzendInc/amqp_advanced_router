package com.azendinc.amqp;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.adapter.AbstractAdaptableMessageListener;
import org.springframework.amqp.rabbit.listener.exception.ListenerExecutionFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MethodInvoker;
import com.azendinc.amqp.annotations.AmqpMessageHandler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * This message listener delegates the listening based
 * on the AmqpMessageHandler annotations
 *
 * @see AmqpMessageHandler
 */
@Component
public class AmqpRoutingMessageListener extends AbstractAdaptableMessageListener {
    @Autowired
    private AmqpMessageHandlerFactory handlerFactory;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        MessageProperties properties = message.getMessageProperties();
        String type = properties.getType();
        List<Object> handlers = handlerFactory.getHandlers(type);
        Object[] arguments = getMethodArguments(message);

        for (Object handler : handlers) {
            Object result = invokeHandlerMethod(handler, arguments, message);
            if (result != null) {
                handleResult(result, message, channel);
            }
        }
    }

    protected Object[] getMethodArguments(Message message) {
        Object convertedMessage = extractMessage(message);
        return new Object[] { convertedMessage };
    }

    protected Object invokeHandlerMethod(Object handler, Object[] arguments, Message originalMessage) throws ListenerExecutionFailedException {
        String methodName = getMethodName(handler);
        String handlerClassName = handler.getClass().getSimpleName();

        MethodInvoker methodInvoker = new MethodInvoker();
        methodInvoker.setTargetObject(handler);
        methodInvoker.setTargetMethod(getMethodName(handler));
        methodInvoker.setArguments(arguments);
        try {
            methodInvoker.prepare();
            return methodInvoker.invoke();
        } catch (InvocationTargetException ex) {
            Throwable targetEx = ex.getTargetException();
            if (targetEx instanceof IOException) {
                throw new AmqpIOException((IOException) targetEx);
            } else {
                throw new ListenerExecutionFailedException(
                        "Listener method '" + handlerClassName + ":" + methodName + "' threw exception",
                        targetEx,
                        originalMessage);
            }
        }
        catch (Exception ex) {
            throw new ListenerExecutionFailedException(
                    "Failed to invoke target method '" + handlerClassName + ":" + methodName, ex, originalMessage);
        }
    }

    protected String getMethodName(Object handler) {
        return handler.getClass().getAnnotation(AmqpMessageHandler.class).methodName();
    }
}
