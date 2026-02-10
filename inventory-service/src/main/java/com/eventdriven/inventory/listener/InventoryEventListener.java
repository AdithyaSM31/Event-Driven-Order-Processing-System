package com.eventdriven.inventory.listener;

import com.eventdriven.inventory.event.OrderCreatedEvent;
import com.eventdriven.inventory.event.PaymentSucceededEvent;
import com.eventdriven.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {

    private final InventoryService inventoryService;

    @KafkaListener(topics = "payment-succeeded-topic", groupId = "inventory-service-group")
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        log.info("Received PaymentSucceededEvent for order: {}", event.getOrderId());
        
        // Convert to OrderCreatedEvent format for processing
        OrderCreatedEvent orderEvent = OrderCreatedEvent.builder()
                .orderId(event.getOrderId())
                .items(event.getItems())
                .build();
        
        inventoryService.reserveInventory(orderEvent);
    }
}
