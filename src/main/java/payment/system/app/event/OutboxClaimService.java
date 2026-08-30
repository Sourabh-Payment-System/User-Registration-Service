package payment.system.app.event;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import payment.system.app.entity.OutboxEvent;
import payment.system.app.enums.OutboxStatus;
import payment.system.app.repository.OutboxEventRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxClaimService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public List<Long> claimPendingEvents(int limit) {

        LocalDateTime now = LocalDateTime.now();

        List<OutboxEvent> events =
                outboxEventRepository
                        .findAndLockPendingEvents(
                                OutboxStatus.PENDING.name(),
                                now,
                                limit
                        );

        if (events.isEmpty()) {
            return List.of();
        }

        for (OutboxEvent event : events) {

            event.setStatus(
                    OutboxStatus.PROCESSING
            );

            event.setProcessingStartedAt(now);
            event.setUpdatedAt(now);
        }

        log.info(
                "Claimed {} outbox events",
                events.size()
        );

        /*
         * Hibernate dirty checking will update
         * the entities when this transaction commits.
         */
        return events.stream()
                .map(OutboxEvent::getId)
                .toList();
    }
}