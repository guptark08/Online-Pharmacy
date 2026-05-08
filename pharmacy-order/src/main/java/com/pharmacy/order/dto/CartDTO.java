package com.pharmacy.order.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartDTO {

    private Long id;
    private Long userId;
    private List<CartItemDTO> items;
    private BigDecimal totalAmount;
    private int totalItems;

    public CartDTO() {}

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public List<CartItemDTO> getItems() { return items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public int getTotalItems() { return totalItems; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setItems(List<CartItemDTO> items) { this.items = items; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
}