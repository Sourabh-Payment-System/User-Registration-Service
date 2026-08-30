package payment.system.app.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import payment.system.app.config.RabbitMQConfig;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserRegistered(
            UserRegisteredEvent event) {

        log.info(
                "Publishing UserRegisteredEvent for userId={}",
                event.userId()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.USER_EXCHANGE,
                RabbitMQConfig.USER_REGISTERED_ROUTING_KEY,
                event
        );

        log.info(
                "UserRegisteredEvent published successfully for userId={}",
                event.userId()
        );
    }
}