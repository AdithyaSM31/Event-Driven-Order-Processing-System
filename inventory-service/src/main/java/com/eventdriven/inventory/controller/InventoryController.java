package com.eventdriven.inventory.controller;

import com.eventdriven.inventory.entity.Inventory;
import com.eventdriven.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    public ResponseEntity<Inventory> getInventory(@PathVariable String productId) {
        Inventory inventory = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(inventory);
    }

    @PostMapping
    public ResponseEntity<Inventory> createOrUpdateInventory(
            @RequestParam String productId,
            @RequestParam String productName,
            @RequestParam Integer quantity) {
        Inventory inventory = inventoryService.createOrUpdateInventory(productId, productName, quantity);
        return ResponseEntity.ok(inventory);
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<Inventory> updateStock(
            @PathVariable String productId,
            @RequestParam Integer quantity) {
        Inventory inventory = inventoryService.updateStock(productId, quantity);
        return ResponseEntity.ok(inventory);
    }
}
