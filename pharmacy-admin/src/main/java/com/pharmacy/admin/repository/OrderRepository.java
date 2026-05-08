package com.pharmacy.admin.repository;

import com.pharmacy.admin.entity.Order;
import com.pharmacy.admin.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByOrderByCreatedAtDesc();
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByUserEmail(String userEmail);
}