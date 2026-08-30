package payment.system.app.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import payment.system.app.entity.OutboxEvent;
import payment.system.app.enums.OutboxStatus;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, Long> {

    @Query(
            value = """
                    SELECT *
                    FROM "user-event".outbox_events
                    WHERE status = :status
                      AND (
                          next_attempt_at IS NULL
                          OR next_attempt_at <= :now
                      )
                    ORDER BY created_at
                    FOR UPDATE SKIP LOCKED
                    LIMIT :limit
                    """,
            nativeQuery = true
    )
    List<OutboxEvent> findAndLockPendingEvents(
            @Param("status") String status,
            @Param("now") LocalDateTime now,
            @Param("limit") int limit
    );

    @Modifying
    @Query(
            value = """
                    UPDATE "user-event".outbox_events
                    SET status = 'PENDING',
                        processing_started_at = NULL,
                        updated_at = :now,
                        next_attempt_at = :now
                    WHERE status = 'PROCESSING'
                      AND processing_started_at < :threshold
                    """,
            nativeQuery = true
    )
    int recoverStuckEvents(
            @Param("threshold") LocalDateTime threshold,
            @Param("now") LocalDateTime now
    );
}