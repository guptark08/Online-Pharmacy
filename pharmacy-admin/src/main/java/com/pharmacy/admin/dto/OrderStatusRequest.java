package com.pharmacy.admin.dto;

import com.pharmacy.admin.enums.OrderStatus;

public class OrderStatusRequest {

    private String status;

    public OrderStatusRequest() {}

    public String getStatus() { return status; }
    public void setStatus(String string) { this.status = string; }
}