package com.pharmacy.admin.dto;

import com.pharmacy.admin.enums.OrderStatus;
import com.pharmacy.admin.enums.PrescriptionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderSummaryDTO {

    private Long id;
    private String userEmail;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private Long prescriptionId;
    private String prescriptionFileName;
    private String prescriptionFileType;
    private PrescriptionStatus prescriptionStatus;

    public OrderSummaryDTO() {}

    public Long getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getPrescriptionId() { return prescriptionId; }
    public String getPrescriptionFileName() { return prescriptionFileName; }
    public String getPrescriptionFileType() { return prescriptionFileType; }
    public PrescriptionStatus getPrescriptionStatus() { return prescriptionStatus; }

    public void setId(Long id) { this.id = id; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setStatus(OrderStatus orderStatus) { this.status = orderStatus; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }
    public void setPrescriptionFileName(String prescriptionFileName) { this.prescriptionFileName = prescriptionFileName; }
    public void setPrescriptionFileType(String prescriptionFileType) { this.prescriptionFileType = prescriptionFileType; }
    public void setPrescriptionStatus(PrescriptionStatus prescriptionStatus) { this.prescriptionStatus = prescriptionStatus; }

}
