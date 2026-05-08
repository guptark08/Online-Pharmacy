package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.enums.PrescriptionStatus;

public class PrescriptionResponseDTO {

    private Long id;
    private Long userId;
    private Long orderId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private PrescriptionStatus status;
    private LocalDateTime uploadedAt;
    private LocalDateTime reviewedAt;
    private String reviewNote;

    // Empty constructor
    public PrescriptionResponseDTO() {}

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getOrderId() { return orderId; }
    public String getFileName() { return fileName; }
    public String getFileUrl() { return fileUrl; }
    public String getFileType() { return fileType; }
    public PrescriptionStatus getStatus() { return status; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public String getReviewNote() { return reviewNote; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public void setStatus(PrescriptionStatus status) { this.status = status; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
}