package com.eventmate.payment.service;

import com.eventmate.payment.dto.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);
    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(PaymentEvent event) {
        if (event == null) {
            log.warn("Skipping publish of null PaymentEvent");
            return;
        }
        String key = event.getPaymentId().toString();
        log.info("Publishing payment event to Kafka. key={}, eventType={}, bookingId={}, paymentId={}",
                key, event.getEventType(), event.getBookingId(), event.getPaymentId());
        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, key, event);
    }
}
