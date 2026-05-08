package com.pharmacy.order.dto;

import com.pharmacy.order.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private Long id;
    private Long userId;
    private List<OrderItemDTO> items;
    private AddressDTO deliveryAddress;
    private Long prescriptionId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String deliverySlot;
    private LocalDateTime createdAt;

    public OrderResponse() {}

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public List<OrderItemDTO> getItems() { return items; }
    public AddressDTO getDeliveryAddress() { return deliveryAddress; }
    public Long getPrescriptionId() { return prescriptionId; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getDeliverySlot() { return deliverySlot; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
    public void setDeliveryAddress(AddressDTO deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setDeliverySlot(String deliverySlot) { this.deliverySlot = deliverySlot; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}