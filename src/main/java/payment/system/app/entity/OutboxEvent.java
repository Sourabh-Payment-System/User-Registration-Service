package payment.system.app.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.Data;
import payment.system.app.enums.OutboxStatus;

@Data
@Entity
@Table(
        name = "outbox_events",
        schema = "user-event",
        indexes = {
                @Index(
                        name = "idx_outbox_status_created",
                        columnList = "status, created_at"
                ),
                @Index(
                        name = "idx_outbox_status_next_attempt",
                        columnList = "status, next_attempt_at"
                )
        }
)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "event_id",
            nullable = false,
            unique = true,
            updatable = false
    )
    private String eventId;

    @Column(
            name = "aggregate_type",
            nullable = false
    )
    private String aggregateType;

    @Column(
            name = "aggregate_id",
            nullable = false
    )
    private Long aggregateId;

    @Column(
            name = "event_type",
            nullable = false
    )
    private String eventType;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    @Column(
            name = "retry_count",
            nullable = false
    )
    private Integer retryCount = 0;

    /**
     * Event should not be retried before this time.
     */
    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Version
    private Long version;
}