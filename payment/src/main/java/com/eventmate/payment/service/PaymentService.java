package com.eventmate.payment.service;

import com.eventmate.payment.dto.CreatePaymentRequest;
import com.eventmate.payment.dto.CreatePaymentResponse;
import com.eventmate.payment.dto.UpdatePaymentStatusRequest;
import com.eventmate.payment.dto.PaymentEvent;
import com.eventmate.payment.model.Payment;
import com.eventmate.payment.model.PaymentStatus;
import com.eventmate.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final int EXPIRE_BATCH_SIZE = 500;

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    public PaymentService(PaymentRepository paymentRepository, PaymentEventProducer paymentEventProducer) {
        this.paymentRepository = paymentRepository;
        this.paymentEventProducer = paymentEventProducer;
    }

    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        log.info("Creating payment for the booking_id {}", request.getBookingId());
        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .userEmail(request.getUserEmail())
                .userName(request.getUserName())
                .userId(request.getUserId())
                .expiresAt(request.getExpiresAt())
                .amount(request.getAmount())
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created with id {} for the booking_id {}", savedPayment.getId(), request.getBookingId());

        return CreatePaymentResponse.builder()
                .paymentId(savedPayment.getId())
                .status(savedPayment.getStatus())
                .build();
    }

    @Transactional
    public void updatePaymentStatus(UpdatePaymentStatusRequest request) {
        UUID paymentId = request.getPaymentId();
        PaymentStatus newStatus = request.getPaymentStatus();

        log.info("Updating payment status for payment_id {} to {}", paymentId, newStatus);

        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
        if (optionalPayment.isEmpty()) {
            log.warn("Payment not found for id {}. Ignoring status update.", paymentId);
            return;
        }

        Payment payment = optionalPayment.get();
        PaymentStatus currentStatus = payment.getStatus();

        if (currentStatus == newStatus) {
            log.info("Payment {} already in status {}. Skipping update.", paymentId, currentStatus);
            return;
        }

        if (currentStatus == PaymentStatus.SUCCESS || currentStatus == PaymentStatus.FAILED
                || currentStatus == PaymentStatus.EXPIRED) {
            log.info("Payment {} already in terminal status {}. Skipping update to {}.",
                    paymentId, currentStatus, newStatus);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = payment.getExpiresAt();
        if (expiresAt != null && expiresAt.isBefore(now) && newStatus == PaymentStatus.SUCCESS) {
            log.info("Payment {} has expired at {}. Overriding SUCCESS status to FAILED.", paymentId, expiresAt);
            newStatus = PaymentStatus.FAILED;
        }

        payment.setStatus(newStatus);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        PaymentEvent event = new PaymentEvent();
        event.setMessageEventId(UUID.randomUUID());
        event.setBookingId(payment.getBookingId());
        event.setUserName(payment.getUserName());
        event.setUserEmail(payment.getUserEmail());
        event.setPaymentId(payment.getId());
        event.setEventType(newStatus == PaymentStatus.SUCCESS ? "payment-success" : "payment-failed");
        paymentEventProducer.publish(event);

        log.info("Payment {} status updated to {} and event published.", paymentId, newStatus);
    }

    /**
     * Scheduled job that runs every minute to mark pending, expired payments as EXPIRED.
     * Idempotent: payments not in PENDING or already past EXPIRED state are never changed here.
     */
    @Scheduled(cron = "0 0 0 * * *") // every minute at second 0
    @Transactional
    public void expirePendingPayments() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Running scheduled payment expiry job at {}", now);

        int totalProcessed = 0;
        while (true) {
            List<Payment> candidates = paymentRepository.findExpiredPendingPayments(
                    PaymentStatus.PENDING,
                    now,
                    PageRequest.of(0, EXPIRE_BATCH_SIZE)
            );

            if (candidates.isEmpty()) {
                break;
            }

            for (Payment payment : candidates) {
                if (payment.getStatus() != PaymentStatus.PENDING) {
                    continue;
                }
                if (payment.getExpiresAt() != null && payment.getExpiresAt().isBefore(now)) {
                    payment.setStatus(PaymentStatus.EXPIRED);
                    payment.setUpdatedAt(now);
                }
            }

            paymentRepository.saveAll(candidates);
            totalProcessed += candidates.size();
        }

        log.info("Completed payment expiry job. Total payments marked EXPIRED: {}", totalProcessed);
    }
}
