package com.eventmate.booking.client;

import com.eventmate.booking.client.dto.PaymentRequest;
import com.eventmate.booking.client.dto.PaymentResponse;
import com.eventmate.booking.exception.PaymentCreationFailure;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PaymentClient {

    private final String BASE_URL = "http://payment/payments";
    private final RestTemplate restTemplate;

    public PaymentClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PaymentResponse createPayment(PaymentRequest request){
        try{
            String url = BASE_URL + "/create";
            return restTemplate.postForObject(url, request, PaymentResponse.class);
        } catch (Exception e){
            throw new PaymentCreationFailure("Failed to create payment", e);
        }
    }
}
