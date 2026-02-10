package com.eventdriven.notification.listener;

import com.eventdriven.notification.event.*;
import com.eventdriven.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "order-created-topic", groupId = "notification-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for order: {}", event.getOrderId());
        notificationService.sendOrderCreatedNotification(event.getOrderId(), event.getUserId());
    }

    @KafkaListener(topics = "payment-succeeded-topic", groupId = "notification-service-group")
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        log.info("Received PaymentSucceededEvent for order: {}", event.getOrderId());
        notificationService.sendPaymentSuccessNotification(event.getOrderId());
    }

    @KafkaListener(topics = "payment-failed-topic", groupId = "notification-service-group")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Received PaymentFailedEvent for order: {}", event.getOrderId());
        notificationService.sendPaymentFailedNotification(event.getOrderId(), event.getReason());
    }

    @KafkaListener(topics = "inventory-reserved-topic", groupId = "notification-service-group")
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("Received InventoryReservedEvent for order: {}", event.getOrderId());
        notificationService.sendInventoryReservedNotification(event.getOrderId());
    }

    @KafkaListener(topics = "inventory-unavailable-topic", groupId = "notification-service-group")
    public void handleInventoryUnavailable(InventoryUnavailableEvent event) {
        log.info("Received InventoryUnavailableEvent for order: {}", event.getOrderId());
        notificationService.sendInventoryUnavailableNotification(event.getOrderId(), event.getReason());
    }
}
