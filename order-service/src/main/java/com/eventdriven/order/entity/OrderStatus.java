package com.eventdriven.order.entity;

public enum OrderStatus {
    PENDING,
    PAYMENT_PROCESSING,
    PAYMENT_CONFIRMED,
    INVENTORY_RESERVED,
    CONFIRMED,
    FAILED,
    CANCELLED
}
