package com.eventdriven.order.service;

import com.eventdriven.order.dto.CreateOrderRequest;
import com.eventdriven.order.dto.OrderItemRequest;
import com.eventdriven.order.dto.OrderResponse;
import com.eventdriven.order.entity.Order;
import com.eventdriven.order.entity.OrderStatus;
import com.eventdriven.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest validOrderRequest;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        OrderItemRequest item1 = OrderItemRequest.builder()
                .productId("PROD001")
                .productName("iPhone 15 Pro")
                .quantity(1)
                .price(BigDecimal.valueOf(999.99))
                .build();

        validOrderRequest = CreateOrderRequest.builder()
                .items(Arrays.asList(item1))
                .build();

        savedOrder = Order.builder()
                .id(1L)
                .userId("user123")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(999.99))
                .build();
    }

    @Test
    void createOrder_WithValidRequest_ShouldReturnOrderResponse() {
        // Arrange
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderResponse response = orderService.createOrder(validOrderRequest, "user123");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo("user123");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(999.99));

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(kafkaTemplate, times(1)).send(anyString(), any());
    }

    @Test
    void createOrder_ShouldCalculateTotalAmountCorrectly() {
        // Arrange
        OrderItemRequest item1 = OrderItemRequest.builder()
                .productId("PROD001")
                .productName("iPhone")
                .quantity(2)
                .price(BigDecimal.valueOf(999.99))
                .build();

        OrderItemRequest item2 = OrderItemRequest.builder()
                .productId("PROD002")
                .productName("AirPods")
                .quantity(1)
                .price(BigDecimal.valueOf(249.99))
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .items(Arrays.asList(item1, item2))
                .build();

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(orderCaptor.capture())).thenReturn(savedOrder);

        // Act
        orderService.createOrder(request, "user123");

        // Assert
        Order capturedOrder = orderCaptor.getValue();
        BigDecimal expectedTotal = BigDecimal.valueOf(2249.97); // (999.99 * 2) + 249.99
        assertThat(capturedOrder.getTotalAmount()).isEqualByComparingTo(expectedTotal);
    }

    @Test
    void getOrderById_WithValidId_ShouldReturnOrder() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(savedOrder));

        // Act
        OrderResponse response = orderService.getOrderById(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void getOrderById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void getOrdersByUserId_ShouldReturnUserOrders() {
        // Arrange
        List<Order> orders = Arrays.asList(savedOrder);
        when(orderRepository.findByUserId("user123")).thenReturn(orders);

        // Act
        List<OrderResponse> responses = orderService.getOrdersByUserId("user123");

        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUserId()).isEqualTo("user123");
        verify(orderRepository, times(1)).findByUserId("user123");
    }

    @Test
    void updateOrderStatus_WithValidId_ShouldUpdateStatus() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(savedOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        // Assert
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.updateOrderStatus(999L, OrderStatus.CONFIRMED))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void createOrder_ShouldPublishKafkaEvent() {
        // Arrange
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        orderService.createOrder(validOrderRequest, "user123");

        // Assert
        verify(kafkaTemplate, times(1)).send(eq("order-created-topic"), any());
    }
}
