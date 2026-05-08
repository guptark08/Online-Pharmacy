package com.pharmacy.admin.dto;

import com.pharmacy.admin.enums.PrescriptionStatus;

public class PrescriptionReviewRequest {

    private PrescriptionStatus status;
    private String reviewNote;

    public PrescriptionReviewRequest() {}

    public PrescriptionStatus getStatus() { return status; }
    public String getReviewNote() { return reviewNote; }

    public void setStatus(PrescriptionStatus status) { this.status = status; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
}