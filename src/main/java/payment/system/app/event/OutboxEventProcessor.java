package payment.system.app.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import payment.system.app.config.RabbitMQConfig;
import payment.system.app.entity.OutboxEvent;
import payment.system.app.enums.OutboxStatus;
import payment.system.app.repository.OutboxEventRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final OutboxStatusService outboxStatusService;


    public void processEvent(Long eventId) {

        OutboxEvent event =
                outboxEventRepository.findById(eventId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Outbox event not found: "
                                                + eventId
                                )
                        );

        if (event.getStatus()
                != OutboxStatus.PROCESSING) {

            log.info(
                    "Skipping event {}. Current status={}",
                    eventId,
                    event.getStatus()
            );

            return;
        }

        try {

            UserRegisteredEvent userEvent =
                    objectMapper.readValue(
                            event.getPayload(),
                            UserRegisteredEvent.class
                    );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.USER_EXCHANGE,
                    RabbitMQConfig.USER_REGISTERED_ROUTING_KEY,
                    userEvent
            );

            /*
             * RabbitMQ send completed successfully.
             */
            outboxStatusService.markPublished(eventId);

            log.info(
                    "Outbox event published successfully. "
                            + "id={}, eventId={}",
                    eventId,
                    event.getEventId()
            );

        } catch (Exception e) {

            outboxStatusService
                    .markPendingForRetry(eventId);

            log.error(
                    "Failed to publish outbox event. id={}",
                    eventId,
                    e
            );

            throw new RuntimeException(
                    "Failed to publish outbox event: "
                            + eventId,
                    e
            );
        }
    }
}