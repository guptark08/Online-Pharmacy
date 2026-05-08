package com.example.demo.dto;

import java.math.BigDecimal;

public class MedicineDTO {

    private Long id;
    private String name;
    private String description;
    private CategoryDTO category;
    private String manufacturer;
    private BigDecimal price;
    private int stock;
    private boolean requiresPrescription;
    private String imageUrl;
    private String dosageInfo;
    private String sideEffects;
    private boolean isActive;

    // Empty constructor
    public MedicineDTO() {}

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public CategoryDTO getCategory() { return category; }
    public String getManufacturer() { return manufacturer; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
    public boolean isRequiresPrescription() { return requiresPrescription; }
    public String getImageUrl() { return imageUrl; }
    public String getDosageInfo() { return dosageInfo; }
    public String getSideEffects() { return sideEffects; }
    public boolean isActive() { return isActive; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(CategoryDTO category) { this.category = category; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
    public void setRequiresPrescription(boolean requiresPrescription) { this.requiresPrescription = requiresPrescription; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setDosageInfo(String dosageInfo) { this.dosageInfo = dosageInfo; }
    public void setSideEffects(String sideEffects) { this.sideEffects = sideEffects; }
    public void setActive(boolean isActive) { this.isActive = isActive; }
}