package com.eventdriven.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    public void sendOrderCreatedNotification(Long orderId, String userId) {
        log.info("📧 Sending 'Order Created' notification for Order #{} to User: {}", orderId, userId);
        // In production: integrate with email service (SendGrid, AWS SES, etc.)
        // or SMS service (Twilio, AWS SNS, etc.)
        simulateEmailSending(userId, "Order Created", 
            String.format("Your order #%d has been created and is being processed.", orderId));
    }

    public void sendPaymentSuccessNotification(Long orderId) {
        log.info("📧 Sending 'Payment Success' notification for Order #{}", orderId);
        simulateEmailSending("customer@example.com", "Payment Successful", 
            String.format("Payment for your order #%d was successful!", orderId));
    }

    public void sendPaymentFailedNotification(Long orderId, String reason) {
        log.error("📧 Sending 'Payment Failed' notification for Order #{}: {}", orderId, reason);
        simulateEmailSending("customer@example.com", "Payment Failed", 
            String.format("Payment for your order #%d failed. Reason: %s", orderId, reason));
    }

    public void sendInventoryReservedNotification(Long orderId) {
        log.info("📧 Sending 'Order Confirmed' notification for Order #{}", orderId);
        simulateEmailSending("customer@example.com", "Order Confirmed", 
            String.format("Your order #%d has been confirmed and will be shipped soon!", orderId));
    }

    public void sendInventoryUnavailableNotification(Long orderId, String reason) {
        log.error("📧 Sending 'Order Failed' notification for Order #{}: {}", orderId, reason);
        simulateEmailSending("customer@example.com", "Order Failed", 
            String.format("Your order #%d could not be fulfilled. Reason: %s", orderId, reason));
    }

    private void simulateEmailSending(String recipient, String subject, String body) {
        log.info("✉️  Email Sent:");
        log.info("   To: {}", recipient);
        log.info("   Subject: {}", subject);
        log.info("   Body: {}", body);
        log.info("─────────────────────────────────────────");
    }
}
