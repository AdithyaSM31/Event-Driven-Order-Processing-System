package com.eventdriven.order.controller;

import com.eventdriven.order.dto.CreateOrderRequest;
import com.eventdriven.order.dto.OrderItemRequest;
import com.eventdriven.order.dto.OrderResponse;
import com.eventdriven.order.entity.OrderStatus;
import com.eventdriven.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private CreateOrderRequest validRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        OrderItemRequest item = OrderItemRequest.builder()
                .productId("PROD001")
                .productName("iPhone 15 Pro")
                .quantity(1)
                .price(BigDecimal.valueOf(999.99))
                .build();

        validRequest = CreateOrderRequest.builder()
                .items(Arrays.asList(item))
                .build();

        orderResponse = OrderResponse.builder()
                .id(1L)
                .userId("user123")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(999.99))
                .items(Collections.emptyList())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createOrder_WithValidRequest_ShouldReturn201() throws Exception {
        // Arrange
        when(orderService.createOrder(any(CreateOrderRequest.class), anyString()))
                .thenReturn(orderResponse);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(999.99));
    }

    @Test
    void createOrder_WithoutUserId_ShouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_WithEmptyItems_ShouldReturn400() throws Exception {
        // Arrange
        CreateOrderRequest emptyRequest = CreateOrderRequest.builder()
                .items(Collections.emptyList())
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user123")
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrder_WithValidId_ShouldReturn200() throws Exception {
        // Arrange
        when(orderService.getOrderById(1L)).thenReturn(orderResponse);

        // Act & Assert
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value("user123"));
    }

    @Test
    void getUserOrders_WithValidUserId_ShouldReturn200() throws Exception {
        // Arrange
        when(orderService.getOrdersByUserId("user123"))
                .thenReturn(Arrays.asList(orderResponse));

        // Act & Assert
        mockMvc.perform(get("/api/orders/user")
                        .header("X-User-Id", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userId").value("user123"));
    }
}
