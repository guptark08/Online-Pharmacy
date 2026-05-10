package com.pharmacy.admin.controller;

import com.pharmacy.admin.dto.PrescriptionReviewRequest;
import com.pharmacy.admin.entity.Prescription;
import com.pharmacy.admin.service.AdminPrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/prescriptions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPrescriptionController {

    @Autowired
    private AdminPrescriptionService adminPrescriptionService;

    @GetMapping
    public ResponseEntity<List<Prescription>> getAllPrescriptions() {
        return ResponseEntity.ok(adminPrescriptionService.getAllPrescriptions());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Prescription>> getPendingPrescriptions() {
        return ResponseEntity.ok(adminPrescriptionService.getPendingPrescriptions());
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<Prescription> reviewPrescription(
            @PathVariable Long id,
            @RequestBody PrescriptionReviewRequest request) {
        return ResponseEntity.ok(
            adminPrescriptionService.reviewPrescription(id, request));
    }
}
