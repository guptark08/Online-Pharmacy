package com.pharmacy.admin.service;

import com.pharmacy.admin.dto.PrescriptionReviewRequest;
import com.pharmacy.admin.entity.Prescription;
import com.pharmacy.admin.enums.PrescriptionStatus;
import com.pharmacy.admin.repository.PrescriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @InjectMocks
    private AdminPrescriptionService adminPrescriptionService;

    @Test
    void getPendingPrescriptionsDelegatesToRepository() {
        Prescription prescription = new Prescription();
        prescription.setStatus(PrescriptionStatus.PENDING);
        when(prescriptionRepository.findByStatus(PrescriptionStatus.PENDING))
            .thenReturn(List.of(prescription));

        List<Prescription> prescriptions = adminPrescriptionService.getPendingPrescriptions();

        assertEquals(1, prescriptions.size());
        verify(prescriptionRepository).findByStatus(PrescriptionStatus.PENDING);
    }

    @Test
    void getAllPrescriptionsReturnsRepositoryData() {
        when(prescriptionRepository.findByOrderByUploadedAtDesc())
            .thenReturn(List.of(new Prescription(), new Prescription()));

        assertEquals(2, adminPrescriptionService.getAllPrescriptions().size());
    }

    @Test
    void reviewPrescriptionUpdatesFields() {
        Prescription prescription = new Prescription();
        prescription.setId(1L);
        prescription.setStatus(PrescriptionStatus.PENDING);

        PrescriptionReviewRequest request = new PrescriptionReviewRequest();
        request.setStatus(PrescriptionStatus.APPROVED);
        request.setReviewNote("Looks good");

        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(prescription));
        when(prescriptionRepository.save(any(Prescription.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Prescription updated = adminPrescriptionService.reviewPrescription(1L, request);

        assertEquals(PrescriptionStatus.APPROVED, updated.getStatus());
        assertEquals("Looks good", updated.getReviewNote());
        assertNotNull(updated.getReviewedAt());
    }

    @Test
    void reviewPrescriptionThrowsWhenMissing() {
        when(prescriptionRepository.findById(42L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> adminPrescriptionService.reviewPrescription(42L, new PrescriptionReviewRequest()));

        assertTrue(ex.getMessage().contains("Prescription not found"));
    }
}
