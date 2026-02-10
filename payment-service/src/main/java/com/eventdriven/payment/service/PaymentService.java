package com.eventdriven.payment.service;

import com.eventdriven.payment.entity.Payment;
import com.eventdriven.payment.entity.PaymentStatus;
import com.eventdriven.payment.event.OrderCreatedEvent;
import com.eventdriven.payment.event.PaymentFailedEvent;
import com.eventdriven.payment.event.PaymentSucceededEvent;
import com.eventdriven.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Random random = new Random();

    @Transactional
    public void processPayment(OrderCreatedEvent event) {
        log.info("Processing payment for order: {}", event.getOrderId());

        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .amount(event.getTotalAmount())
                .status(PaymentStatus.PROCESSING)
                .paymentMethod("CREDIT_CARD")
                .build();

        paymentRepository.save(payment);

        // Simulate payment processing (90% success rate)
        boolean paymentSuccess = random.nextInt(100) < 90;

        if (paymentSuccess) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.setTransactionId(UUID.randomUUID().toString());
            paymentRepository.save(payment);

            PaymentSucceededEvent successEvent = PaymentSucceededEvent.builder()
                    .orderId(event.getOrderId())
                    .paymentId(payment.getTransactionId())
                    .items(event.getItems())
                    .build();

            kafkaTemplate.send("payment-succeeded-topic", successEvent);
            log.info("Payment succeeded for order: {}", event.getOrderId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Insufficient funds");
            paymentRepository.save(payment);

            PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                    .orderId(event.getOrderId())
                    .reason("Insufficient funds")
                    .build();

            kafkaTemplate.send("payment-failed-topic", failedEvent);
            log.info("Payment failed for order: {}", event.getOrderId());
        }
    }

    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
    }
}
