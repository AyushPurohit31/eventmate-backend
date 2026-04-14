package com.eventmate.payment.repository;

import com.eventmate.payment.model.Payment;
import com.eventmate.payment.model.PaymentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.expiresAt < :now ORDER BY p.expiresAt ASC")
    List<Payment> findExpiredPendingPayments(PaymentStatus status, LocalDateTime now, Pageable pageable);
}
