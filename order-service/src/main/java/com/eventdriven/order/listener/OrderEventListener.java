package com.eventdriven.order.listener;

import com.eventdriven.order.entity.OrderStatus;
import com.eventdriven.order.event.*;
import com.eventdriven.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final OrderService orderService;

    @KafkaListener(topics = "payment-succeeded-topic", groupId = "order-service-group")
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        log.info("Received PaymentSucceededEvent for order: {}", event.getOrderId());
        orderService.updateOrderStatus(event.getOrderId(), OrderStatus.PAYMENT_CONFIRMED);
    }

    @KafkaListener(topics = "payment-failed-topic", groupId = "order-service-group")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Received PaymentFailedEvent for order: {}", event.getOrderId());
        orderService.updateOrderStatus(event.getOrderId(), OrderStatus.FAILED);
    }

    @KafkaListener(topics = "inventory-reserved-topic", groupId = "order-service-group")
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("Received InventoryReservedEvent for order: {}", event.getOrderId());
        orderService.updateOrderStatus(event.getOrderId(), OrderStatus.CONFIRMED);
    }

    @KafkaListener(topics = "inventory-unavailable-topic", groupId = "order-service-group")
    public void handleInventoryUnavailable(InventoryUnavailableEvent event) {
        log.info("Received InventoryUnavailableEvent for order: {}", event.getOrderId());
        orderService.updateOrderStatus(event.getOrderId(), OrderStatus.FAILED);
    }
}
