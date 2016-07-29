package com.azendinc.amqp;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * This component is the single point where all messages are sent
 */
@Component
public class AmqpMessageSender {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Sends a basic hashmap data package with a message
     *
     * @param type type of message to send
     * @param data hashmap of data to send
     */
    public void sendMessage(String type, Map<String, String> data) {
        rabbitTemplate.convertAndSend(type, data);
    }
}
