package com.example.demo.dto;

import java.math.BigDecimal;

public class MedicineRequest {

    private String name;
    private String description;
    private Long categoryId;
    private String manufacturer;
    private BigDecimal price;
    private int stock;
    private boolean requiresPrescription;
    private String imageUrl;
    private String dosageInfo;
    private String sideEffects;

    // Empty constructor
    public MedicineRequest() {}

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Long getCategoryId() { return categoryId; }
    public String getManufacturer() { return manufacturer; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
    public boolean isRequiresPrescription() { return requiresPrescription; }
    public String getImageUrl() { return imageUrl; }
    public String getDosageInfo() { return dosageInfo; }
    public String getSideEffects() { return sideEffects; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
    public void setRequiresPrescription(boolean requiresPrescription) { this.requiresPrescription = requiresPrescription; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setDosageInfo(String dosageInfo) { this.dosageInfo = dosageInfo; }
    public void setSideEffects(String sideEffects) { this.sideEffects = sideEffects; }
}