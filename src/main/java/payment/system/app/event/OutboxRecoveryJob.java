package payment.system.app.event;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import payment.system.app.repository.OutboxEventRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRecoveryJob {

    private final OutboxEventRepository outboxEventRepository;


    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void recoverStuckEvents() {

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime threshold =
                now.minusMinutes(5);

        int recovered =
                outboxEventRepository
                        .recoverStuckEvents(
                                threshold,
                                now
                        );

        if (recovered > 0) {

            log.warn(
                    "Recovered {} stuck PROCESSING "
                            + "outbox events",
                    recovered
            );
        }
    }
}