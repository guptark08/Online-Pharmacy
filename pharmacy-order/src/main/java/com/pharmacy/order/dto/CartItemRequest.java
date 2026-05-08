package com.pharmacy.order.dto;

import java.math.BigDecimal;

public class CartItemRequest {

    private Long medicineId;
    private String medicineName;
    private BigDecimal price;
    private int quantity;
    private boolean requiresPrescription;

    public CartItemRequest() {}

    public Long getMedicineId() { return medicineId; }
    public String getMedicineName() { return medicineName; }
    public BigDecimal getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public boolean isRequiresPrescription() { return requiresPrescription; }

    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setRequiresPrescription(boolean requiresPrescription) { this.requiresPrescription = requiresPrescription; }
}