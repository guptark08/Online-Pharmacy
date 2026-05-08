package com.pharmacy.admin.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicines")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    private String manufacturer;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private int stock;

    private boolean requiresPrescription;

    private String imageUrl;
    private String dosageInfo;
    private String sideEffects;

    @Column(nullable = false)
    private boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Medicine() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public String getManufacturer() { return manufacturer; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
    public boolean isRequiresPrescription() { return requiresPrescription; }
    public String getImageUrl() { return imageUrl; }
    public String getDosageInfo() { return dosageInfo; }
    public String getSideEffects() { return sideEffects; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(Category category) { this.category = category; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
    public void setRequiresPrescription(boolean requiresPrescription) { this.requiresPrescription = requiresPrescription; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setDosageInfo(String dosageInfo) { this.dosageInfo = dosageInfo; }
    public void setSideEffects(String sideEffects) { this.sideEffects = sideEffects; }
    public void setActive(boolean isActive) { this.isActive = isActive; }
}