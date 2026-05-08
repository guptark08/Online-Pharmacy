package com.pharmacy.order.dto;

import java.math.BigDecimal;

public class CartItemDTO {

    private Long id;
    private Long medicineId;
    private String medicineName;
    private BigDecimal price;
    private int quantity;
    private BigDecimal subtotal;
    private boolean requiresPrescription;

    public CartItemDTO() {}

    public Long getId() { return id; }
    public Long getMedicineId() { return medicineId; }
    public String getMedicineName() { return medicineName; }
    public BigDecimal getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public BigDecimal getSubtotal() { return subtotal; }
    public boolean isRequiresPrescription() { return requiresPrescription; }

    public void setId(Long id) { this.id = id; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public void setRequiresPrescription(boolean requiresPrescription) { this.requiresPrescription = requiresPrescription; }
}