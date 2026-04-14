package com.eventmate.payment.controller;

import com.eventmate.payment.dto.CreatePaymentRequest;
import com.eventmate.payment.dto.CreatePaymentResponse;
import com.eventmate.payment.dto.UpdatePaymentStatusRequest;
import com.eventmate.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    public ResponseEntity<CreatePaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) {
        log.info("Received request to create payment for booking_id {}", request.getBookingId());
        CreatePaymentResponse response = paymentService.createPayment(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/update-status")
    public ResponseEntity<Void> updatePaymentStatus(@RequestBody UpdatePaymentStatusRequest request) {
        log.info("Received request to update payment status for payment_id {} to {}",
                request.getPaymentId(), request.getPaymentStatus());
        paymentService.updatePaymentStatus(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
