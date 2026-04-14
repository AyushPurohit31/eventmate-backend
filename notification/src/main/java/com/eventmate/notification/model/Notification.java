package com.eventmate.notification.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_status", columnList = "status, next_attempt_at")
})
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "notification_key", unique = true, nullable = false)
    private UUID idempotencyKey;

    private String userEmail;

    private String userName;

    private UUID bookingId;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private Integer retryCount;

    private LocalDateTime next_attempt_at;

    @Column(columnDefinition = "TEXT")
    private String last_error;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
