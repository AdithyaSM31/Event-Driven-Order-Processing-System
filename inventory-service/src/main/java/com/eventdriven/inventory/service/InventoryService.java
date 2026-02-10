package com.eventdriven.inventory.service;

import com.eventdriven.inventory.entity.Inventory;
import com.eventdriven.inventory.event.*;
import com.eventdriven.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    @CacheEvict(value = "inventory", allEntries = true)
    public void reserveInventory(OrderCreatedEvent event) {
        log.info("Reserving inventory for order: {}", event.getOrderId());

        try {
            for (OrderItemEvent item : event.getItems()) {
                Inventory inventory = inventoryRepository.findByProductId(item.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

                if (!inventory.hasAvailableStock(item.getQuantity())) {
                    throw new RuntimeException("Insufficient stock for product: " + item.getProductName());
                }

                inventory.reserveStock(item.getQuantity());
                inventoryRepository.save(inventory);
            }

            InventoryReservedEvent reservedEvent = InventoryReservedEvent.builder()
                    .orderId(event.getOrderId())
                    .build();

            kafkaTemplate.send("inventory-reserved-topic", reservedEvent);
            log.info("Inventory reserved for order: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("Failed to reserve inventory for order: {}", event.getOrderId(), e);

            InventoryUnavailableEvent unavailableEvent = InventoryUnavailableEvent.builder()
                    .orderId(event.getOrderId())
                    .reason(e.getMessage())
                    .build();

            kafkaTemplate.send("inventory-unavailable-topic", unavailableEvent);
        }
    }

    @Cacheable(value = "inventory", key = "#productId")
    public Inventory getInventoryByProductId(String productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
    }

    @Transactional
    @CacheEvict(value = "inventory", key = "#productId")
    public Inventory updateStock(String productId, Integer quantity) {
        Inventory inventory = getInventoryByProductId(productId);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        return inventoryRepository.save(inventory);
    }

    @Transactional
    @CacheEvict(value = "inventory", allEntries = true)
    public Inventory createOrUpdateInventory(String productId, String productName, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElse(Inventory.builder()
                        .productId(productId)
                        .productName(productName)
                        .availableQuantity(0)
                        .reservedQuantity(0)
                        .build());

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        return inventoryRepository.save(inventory);
    }
}
