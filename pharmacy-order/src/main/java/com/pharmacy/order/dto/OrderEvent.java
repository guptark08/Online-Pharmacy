package com.pharmacy.order.dto;

import com.pharmacy.order.enums.OrderStatus;
import java.io.Serializable;

public class OrderEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String userEmail;
    private OrderStatus status;

    public OrderEvent() {}

    public OrderEvent(Long orderId, String userEmail, OrderStatus status) {
        this.orderId = orderId;
        this.userEmail = userEmail;
        this.status = status;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
