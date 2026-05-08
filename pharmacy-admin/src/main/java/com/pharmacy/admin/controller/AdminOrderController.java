package com.pharmacy.admin.controller;

import com.pharmacy.admin.dto.OrderStatusRequest;
import com.pharmacy.admin.dto.OrderSummaryDTO;
import com.pharmacy.admin.service.AdminOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    @Autowired
    private AdminOrderService adminOrderService;

    @GetMapping
    public ResponseEntity<List<OrderSummaryDTO>> getAllOrders() {
        return ResponseEntity.ok(adminOrderService.getAllOrders());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderSummaryDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody OrderStatusRequest request) {
        return ResponseEntity.ok(
            adminOrderService.updateOrderStatus(id, request));
    }
}