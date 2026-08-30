package payment.system.app.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String USER_EXCHANGE =
            "payment.user.exchange";

    public static final String USER_REGISTERED_ROUTING_KEY =
            "user.registered";


    /**
     * Exchange used for publishing user-related events.
     *
     * Flow:
     *
     * User-Registration-Service
     *        |
     *        | user.registered
     *        v
     * payment.user.exchange
     */
    @Bean
    public TopicExchange userExchange() {

        return new TopicExchange(
                USER_EXCHANGE,
                true,
                false
        );
    }


    @Bean
    public MessageConverter messageConverter() {

        return new Jackson2JsonMessageConverter();
    }
}