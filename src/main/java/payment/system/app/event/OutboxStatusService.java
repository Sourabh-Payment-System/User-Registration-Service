package payment.system.app.event;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import payment.system.app.entity.OutboxEvent;
import payment.system.app.enums.OutboxStatus;
import payment.system.app.repository.OutboxEventRepository;

@Service
@RequiredArgsConstructor
public class OutboxStatusService {

    private static final int MAX_RETRIES = 5;

    private static final long INITIAL_BACKOFF_SECONDS = 10;

    private static final long MAX_BACKOFF_SECONDS = 300;

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public void markPublished(Long eventId) {

        OutboxEvent event =
                outboxEventRepository.findById(eventId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Outbox event not found: "
                                                + eventId
                                )
                        );

        LocalDateTime now = LocalDateTime.now();

        event.setStatus(
                OutboxStatus.PUBLISHED
        );

        event.setProcessedAt(now);
        event.setUpdatedAt(now);
        event.setProcessingStartedAt(null);
        event.setNextAttemptAt(null);
        throw new RuntimeException();
    }

    @Transactional
    public void markPendingForRetry(Long eventId) {

        OutboxEvent event =
                outboxEventRepository.findById(eventId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Outbox event not found: "
                                                + eventId
                                )
                        );

        LocalDateTime now = LocalDateTime.now();

        int currentRetryCount =
                event.getRetryCount() == null
                        ? 0
                        : event.getRetryCount();

        int newRetryCount =
                currentRetryCount + 1;

        event.setRetryCount(newRetryCount);
        event.setUpdatedAt(now);
        event.setProcessingStartedAt(null);

        if (newRetryCount > MAX_RETRIES) {

            /*
             * We don't want the scheduler to retry
             * this event forever.
             */
            event.setStatus(
                    OutboxStatus.FAILED
            );

            event.setNextAttemptAt(null);

            return;
        }

        long backoffSeconds =
                calculateBackoff(newRetryCount);

        LocalDateTime nextAttempt =
                now.plusSeconds(backoffSeconds);

        event.setStatus(
                OutboxStatus.PENDING
        );

        event.setNextAttemptAt(nextAttempt);
    }

    private long calculateBackoff(int retryCount) {

        long backoff =
                INITIAL_BACKOFF_SECONDS
                        * (1L << (retryCount - 1));

        return Math.min(
                backoff,
                MAX_BACKOFF_SECONDS
        );
    }
}