package com.example.demo.entitty;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_batches")
public class InventoryBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(nullable = false)
    private String batchNumber;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private LocalDate manufactureDate;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime addedAt;

    // Empty constructor — required by JPA
    public InventoryBatch() {}

    // Getters
    public Long getId() { return id; }
    public Medicine getMedicine() { return medicine; }
    public String getBatchNumber() { return batchNumber; }
    public int getQuantity() { return quantity; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public LocalDate getManufactureDate() { return manufactureDate; }
    public LocalDateTime getAddedAt() { return addedAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public void setManufactureDate(LocalDate manufactureDate) { this.manufactureDate = manufactureDate; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}