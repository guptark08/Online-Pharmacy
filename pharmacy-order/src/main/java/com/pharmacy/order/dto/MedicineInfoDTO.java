package com.pharmacy.order.dto;

import java.math.BigDecimal;

public class MedicineInfoDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private boolean requiresPrescription;
    private int stock;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public boolean isRequiresPrescription() { return requiresPrescription; }
    public void setRequiresPrescription(boolean requiresPrescription) { this.requiresPrescription = requiresPrescription; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}
