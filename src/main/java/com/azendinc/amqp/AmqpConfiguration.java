package com.azendinc.billassist.injectables.messaging;

import com.azendinc.billassist.constants.ConfigPropertyNames;
import com.azendinc.billassist.constants.MessageTypes;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.amqp.core.BindingBuilder.bind;

/**
 * Configuration class for RabbitMQ system
 */
@Configuration
public class AmqpConfiguration {
    /**
     * Class to wrap the HashMap<String, String> object
     * so that it is easy to refer to by class
     */
    public class JsonMessageBody extends HashMap<String, String> { }

    @Value(ConfigPropertyNames.RABBITMQ_DEFAULT_EXCHANGE)
    private String exchangeName;

    @Value(ConfigPropertyNames.RABBITMQ_DEFAULT_QUEUE)
    private String queueName;

    @Value(ConfigPropertyNames.RABBITMQ_USERNAME)
    private String connectionUsername;

    @Value(ConfigPropertyNames.RABBITMQ_PASSWORD)
    private String connectionPassword;

    @Value(ConfigPropertyNames.RABBITMQ_SERVER_HOSTNAME)
    private String serverHostName;

    @Value(ConfigPropertyNames.RABBITMQ_CHANNEL_CACHE_SIZE)
    private String channelCacheSize;

    /**
     * We flag the massages we are listening for in the
     * enumerator and then they are collected and returned
     * here
     *
     * @return list of messages to listen for
     * @see MessageTypes
     */
     protected List<String> routingKeys() {
         return Arrays.stream(MessageTypes.class.getEnumConstants())
            .filter(MessageTypes::isListened)
            .map(MessageTypes::toString)
            .collect(Collectors.toList());
     }

    /**
     * Sets up the ListenerContainer and MessageListeners
     *
     * @param connectionFactory Injected factory for connections
     * @param messageRouter injected MessageListener
     * @return the container
     */
    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                             AmqpRoutingMessageListener messageRouter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener(messageRouter);
        messageRouter.setMessageConverter(jsonMessageConverter());
        return container;
    }

    /**
     * Sets up the Jackson2JsonMessageConverter as the default
     * MessageConverter for message bodies
     *
     * @return the message converter
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        DefaultClassMapper typeMapper = new DefaultClassMapper();

        typeMapper.setDefaultType(HashMap.class);

        final Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setClassMapper(typeMapper);
        return converter;
    }

    /**
     * Configures the connection to the messaging
     * sub-system
     *
     * @return The connection factory
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(serverHostName);
        factory.setUsername(connectionUsername);
        factory.setPassword(connectionPassword);
        factory.setChannelCacheSize(Integer.parseInt(channelCacheSize));
        return factory;
    }

    /**
     * Configures the rabbit template used to send messages
     *
     * @return the RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    /**
     * Configures the queue for listening for this micro-service
     * NOTE: this is used to create the bindings
     *
     * @return the queue for this micro-service
     */
    @Bean
    public Queue queue() {
        return new Queue(queueName, true, false, false);
    }

    /**
     * Configures the exchange for the micro-service
     * NOTE: this is used to create the bindings
     *
     * @return the exchange that feeds the queue
     */
    @Bean
    public Exchange exchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    /**
     * Sets up the bindings for the messaging sub-system
     *
     * @param queue the queue to bind
     * @param exchange the exchange to bind with the queue
     * @return the list of bindings
     */
    @Bean
    public List<Binding> bindings(Queue queue, Exchange exchange) {
        return routingKeys().stream().collect(
            ArrayList::new,
            (ArrayList<Binding> bindings, String routeKey) ->
                bindings.add(createBinding(queue, exchange, routeKey)),
            ArrayList::addAll
        );
    }

    /**
     * Creates a single binding for the queue and exchange based on
     * a route key for filtering.
     *
     * @param queue the queue to bind
     * @param exchange the exchange to bind
     * @param routeKey the route key to filter on
     * @return the binding
     */
    private Binding createBinding(Queue queue, Exchange exchange, String routeKey) {
        return bind(queue).to(exchange).with(routeKey).noargs();
    }
}
