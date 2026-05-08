package com.pharmacy.admin.service;

import com.pharmacy.admin.dto.PrescriptionReviewRequest;
import com.pharmacy.admin.entity.Prescription;
import com.pharmacy.admin.enums.PrescriptionStatus;
import com.pharmacy.admin.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminPrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    public List<Prescription> getPendingPrescriptions() {
        return prescriptionRepository.findByStatus(PrescriptionStatus.PENDING);
    }

    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.findByOrderByUploadedAtDesc();
    }

    public Prescription reviewPrescription(Long id,
                                            PrescriptionReviewRequest request) {
        Prescription prescription = prescriptionRepository.findById(id)
            .orElseThrow(() ->
                new RuntimeException("Prescription not found: " + id));
        prescription.setStatus(request.getStatus());
        prescription.setReviewNote(request.getReviewNote());
        prescription.setReviewedAt(LocalDateTime.now());
        return prescriptionRepository.save(prescription);
    }
}
