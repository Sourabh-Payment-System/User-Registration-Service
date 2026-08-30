package payment.system.app.event;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private static final int BATCH_SIZE = 100;

    private final OutboxClaimService outboxClaimService;

    private final OutboxEventProcessor outboxEventProcessor;

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<Long> eventIds =
                outboxClaimService
                        .claimPendingEvents(
                                BATCH_SIZE
                        );

        if (eventIds.isEmpty()) {
            return;
        }

        log.info(
                "Processing {} claimed outbox events",
                eventIds.size()
        );

        for (Long eventId : eventIds) {

            try {

                outboxEventProcessor
                        .processEvent(eventId);

            } catch (Exception e) {

                /*
                 * Processor has already changed the event
                 * back to PENDING and calculated the next
                 * retry time.
                 *
                 * Continue processing the remaining events.
                 */
                log.error(
                        "Failed processing outbox event. id={}",
                        eventId
                );
            }
        }
    }
}