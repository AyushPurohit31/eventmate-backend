package com.eventmate.notification.repository;

import com.eventmate.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    boolean existsByIdempotencyKey(UUID idempotencyKey);

    @Query(value = """
        SELECT * FROM notifications
        WHERE status = 'PENDING'
          AND next_attempt_at <= NOW()
        ORDER BY created_at
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<Notification> fetchBatchForUpdate(@Param("limit") int limit);

    @Modifying
    @Query(value = """
        UPDATE notifications
        SET status = 'PROCESSING',
            updated_at = NOW()
        WHERE id IN (:ids)
        """, nativeQuery = true)
    void markProcessing(@Param("ids") List<UUID> ids);

    @Modifying
    @Query(value = """
        UPDATE notifications
        SET status = 'SENT',
            updated_at = NOW()
        WHERE id = :id
        """, nativeQuery = true)
    void markSent(@Param("id") UUID id);

    @Modifying
    @Query(value = """
        UPDATE notifications
        SET status = 'PENDING',
            retry_count = :retryCount,
            next_attempt_at = :nextAttemptAt,
            last_error = :error,
            updated_at = NOW()
        WHERE id = :id
        """, nativeQuery = true)
    void markForRetry(@Param("id") UUID id,
                      @Param("retryCount") int retryCount,
                      @Param("nextAttemptAt") LocalDateTime nextAttemptAt,
                      @Param("error") String error);

    @Modifying
    @Query(value = """
        UPDATE notifications
        SET status = 'FAILED',
            last_error = :error,
            updated_at = NOW()
        WHERE id = :id
        """, nativeQuery = true)
    void markFailed(@Param("id") UUID id,
                    @Param("error") String error);

    @Modifying
    @Query(value = """
        UPDATE notifications
        SET status = 'PENDING',
            updated_at = NOW()
        WHERE status = 'PROCESSING'
          AND updated_at < NOW() - INTERVAL '5 minutes'
        """, nativeQuery = true)
    int resetStuckProcessing();

}