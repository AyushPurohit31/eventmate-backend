package com.eventmate.notification.service;

import com.eventmate.notification.dto.PaymentEvent;
import com.eventmate.notification.model.Notification;
import com.eventmate.notification.model.NotificationStatus;
import com.eventmate.notification.model.NotificationType;
import com.eventmate.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    private static final String SUCCESS_SUBJECT = "Payment Success for Booking ID: %s";
    private static final String SUCCESS_BODY = "Dear %s,\n\nYour payment for booking ID %s was successful. Thank you for your purchase!\n\nBest regards,\nEventMate Team";
    private static final String FAILURE_SUBJECT = "Payment Failure for Booking ID: %s";
    private static final String FAILURE_BODY = "Dear %s,\n\nUnfortunately, your payment for booking ID %s failed. Please try again or contact support for assistance.\n\nBest regards,\nEventMate Team";

    public PaymentEventListener(NotificationRepository notificationRepository,
                                NotificationService notificationService) {
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-service", containerFactory = "paymentKafkaListenerContainerFactory")
    public void handlePaymentEvent(PaymentEvent event) {
         log.info("RECEIVED PAYMENT EVENT: {}", event);
         if (event == null) {
             log.warn("Received null PaymentEvent. Skipping.");
             return;
         }
         String eventType = event.getEventType();
         log.info("Received payment event: paymentId={}, bookingId={}, eventType={}",
                 event.getPaymentId(), event.getBookingId(), eventType);

        NotificationType type = switch (eventType) {
            case "payment-success" -> NotificationType.PAYMENT_SUCCESS;
            case "payment-failed" -> NotificationType.PAYMENT_FAILED;
            default -> {
                log.warn("Unknown payment event type: {}. Skipping.", eventType);
                yield null;
            }
        };

        String subjectString = type==NotificationType.PAYMENT_SUCCESS ? SUCCESS_SUBJECT : FAILURE_SUBJECT;
        String bodyString = type==NotificationType.PAYMENT_SUCCESS ? SUCCESS_BODY : FAILURE_BODY;
        String subject = String.format(subjectString, event.getBookingId());
        String body = String.format(bodyString, event.getUserName(), event.getBookingId());

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

    private void saveNotification(PaymentEvent event, NotificationType type, String subject, String body, NotificationStatus status) {
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
