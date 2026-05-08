package com.pharmacy.admin.entity;

import com.pharmacy.admin.enums.PrescriptionStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private Long orderId;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private String fileName;

    private String fileType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrescriptionStatus status = PrescriptionStatus.PENDING;

    private LocalDateTime uploadedAt;
    private LocalDateTime reviewedAt;
    private String reviewNote;

    public Prescription() {}

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getOrderId() { return orderId; }
    public String getFileUrl() { return fileUrl; }
    public String getFileName() { return fileName; }
    public String getFileType() { return fileType; }
    public PrescriptionStatus getStatus() { return status; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public String getReviewNote() { return reviewNote; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public void setStatus(PrescriptionStatus status) { this.status = status; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
}