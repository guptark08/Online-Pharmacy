package com.pharmacy.order.controller;

import com.pharmacy.order.dto.*;
import com.pharmacy.order.security.JwtUtil;
import com.pharmacy.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/checkout/start")
    public ResponseEntity<OrderResponse> checkout(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CheckoutRequest request) {
        String email = extractEmail(authHeader);
        return ResponseEntity.ok(orderService.checkout(email, request));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @RequestHeader("Authorization") String authHeader) {
        String email = extractEmail(authHeader);
        return ResponseEntity.ok(orderService.getMyOrders(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        String email = extractEmail(authHeader);
        return ResponseEntity.ok(orderService.getOrderById(id, email));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        String email = extractEmail(authHeader);
        return ResponseEntity.ok(orderService.cancelOrder(id, email));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody OrderStatusUpdateRequest request) {
        // For simplicity, role check might happen at security filter level via default route
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getStatus()));
    }

    @PostMapping("/{id}/payment")
    public ResponseEntity<PaymentResponse> payOrder(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody PaymentRequest paymentRequest) {
        String email = extractEmail(authHeader);
        paymentRequest.setOrderId(id);
        return ResponseEntity.ok(orderService.processPayment(paymentRequest, email));
    }

    private String extractEmail(String authHeader) {
        return jwtUtil.extractUsername(authHeader.substring(7));
    }
}