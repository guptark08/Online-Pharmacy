package com.pharmacy.admin.dto;

import java.time.LocalDate;

public class BatchRequest {

    private String batchNumber;
    private int quantity;
    private LocalDate expiryDate;
    private LocalDate manufactureDate;

    public BatchRequest() {}

    public String getBatchNumber() { return batchNumber; }
    public int getQuantity() { return quantity; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public LocalDate getManufactureDate() { return manufactureDate; }

    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public void setManufactureDate(LocalDate manufactureDate) { this.manufactureDate = manufactureDate; }
}