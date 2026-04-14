package com.eventmate.notification.service;

import com.eventmate.notification.dto.BookingEvent;
import com.eventmate.notification.dto.PaymentEvent;
import com.eventmate.notification.model.Notification;
import com.eventmate.notification.model.NotificationStatus;
import com.eventmate.notification.model.NotificationType;
import com.eventmate.notification.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BookingEventListener {

    private static final Logger log = LoggerFactory.getLogger(BookingEventListener.class);

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    private static final String SUCCESS_SUBJECT = "Booking Confirmed: %s";
    private static final String SUCCESS_BODY = "Dear %s,\n\nYour booking %s has been confirmed. Enjoy your event!\n\nBest regards,\nEventMate Team";

    private static final String FAILURE_SUBJECT = "Booking Failed: %s";
    private static final String FAILURE_BODY = "Dear %s,\n\nUnfortunately, your booking %s could not be completed. Please try again or contact support.\n\nBest regards,\nEventMate Team";

    public BookingEventListener(NotificationRepository notificationRepository,
                                NotificationService notificationService) {
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "booking-events", groupId = "notification-service", containerFactory = "bookingKafkaListenerContainerFactory")
    @Transactional
    public void handleBookingEvents(BookingEvent event) {
        log.info("RECEIVED BOOKING EVENT: {}", event);

        if (event == null) {
            log.warn("Received null BookingEvent. Skipping.");
            return;
        }

        String eventType = event.getEventType();
        log.info("Received booking event: bookingId={}, seatReservationId={}, eventType={}",
                event.getBookingId(), event.getSeatReservationId(), eventType);

        NotificationType type = switch (eventType) {
            case "booking-confirmed" -> NotificationType.BOOKING_SUCCESS;
            case "booking-failed" -> NotificationType.BOOKING_FAILED;
            default -> {
                log.warn("Unknown booking event type: {}. Skipping.", eventType);
                yield null;
            }
        };
        if (type == null) {
            return;
        }

        String subjectTemplate = (type == NotificationType.BOOKING_SUCCESS)
                ? SUCCESS_SUBJECT
                : FAILURE_SUBJECT;
        String bodyTemplate = (type == NotificationType.BOOKING_SUCCESS)
                ? SUCCESS_BODY
                : FAILURE_BODY;

        String subject = String.format(subjectTemplate, event.getBookingId());
        String body = String.format(bodyTemplate, event.getUserName(), event.getBookingId());

        UUID idempotencyKey = event.getMessageEventId();
        if (notificationRepository.existsByIdempotencyKey(idempotencyKey)) {
            log.info("Duplicate event detected. Skipping. key={}", idempotencyKey);
            return;
        }

        try {
            notificationService.sendEmail(event.getUserEmail(), subject, body);
            log.info("Email sent successfully for bookingId={}", event.getBookingId());
            saveNotification(event, type, subject, body, NotificationStatus.SENT);
        } catch (Exception ex) {
            log.error("Email failed. Storing for retry. bookingId={}, error={}", event.getBookingId(), ex.getMessage());
            saveNotification(event, type, subject, body, NotificationStatus.PENDING);
        }
    }

    private void saveNotification(BookingEvent event, NotificationType type, String subject, String body, NotificationStatus status) {
        Notification notification = new Notification();
        notification.setIdempotencyKey(event.getMessageEventId());
        notification.setUserEmail(event.getUserEmail());
        notification.setUserName(event.getUserName());
        notification.setBookingId(event.getBookingId());
        notification.setNotificationType(type);
        notification.setStatus(status);
        notification.setRetryCount(0);
        notification.setSubject(subject);
        notification.setBody(body);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}
