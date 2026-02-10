package com.eventdriven.order.repository;

import com.eventdriven.order.entity.Order;
import com.eventdriven.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(String userId);
    List<Order> findByStatus(OrderStatus status);
}
