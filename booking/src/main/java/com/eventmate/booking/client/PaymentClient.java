package com.eventmate.booking.client;

import com.eventmate.booking.client.dto.PaymentRequest;
import com.eventmate.booking.client.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", path = "/api/payments")
public interface PaymentClient {

    @PostMapping("/create")
    PaymentResponse createPayment(@RequestBody PaymentRequest request);
}
