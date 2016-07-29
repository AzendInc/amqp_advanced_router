package com.azendinc.amqp.constants;

/**
 * This contains the static strings for representing the path to the configuration values
 * stored in the applications config files
 */
public class ConfigPropertyNames {

    private ConfigPropertyNames() { }

    // RabbitMQ
    public static final String RABBITMQ_DEFAULT_QUEUE = "${rabbitmq.default-queue}";
    public static final String RABBITMQ_DEFAULT_EXCHANGE = "${rabbitmq.default-exchange}";
    public static final String RABBITMQ_CHANNEL_CACHE_SIZE = "${rabbitmq.channel-cache-size}";
    public static final String RABBITMQ_USERNAME = "${rabbitmq.username}";
    public static final String RABBITMQ_PASSWORD = "${rabbitmq.password}";
    public static final String RABBITMQ_SERVER_HOSTNAME = "${rabbitmq.server-hostname}";
}


