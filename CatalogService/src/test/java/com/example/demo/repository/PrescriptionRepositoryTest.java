package com.example.demo.repository;

import com.example.demo.TestApplication;
import com.example.demo.entitty.Prescription;
import com.example.demo.enums.PrescriptionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PrescriptionRepositoryTest {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Test
    void findByUserId_ShouldReturnUserPrescriptions() {
        // Given
        Prescription prescription1 = new Prescription();
        prescription1.setUserId(1L);
        prescription1.setFileName("prescription1.pdf");
        prescription1.setFileUrl("uploads/prescription1.pdf");
        prescription1.setStatus(PrescriptionStatus.PENDING);

        Prescription prescription2 = new Prescription();
        prescription2.setUserId(1L);
        prescription2.setFileName("prescription2.pdf");
        prescription2.setFileUrl("uploads/prescription2.pdf");
        prescription2.setStatus(PrescriptionStatus.APPROVED);

        Prescription prescription3 = new Prescription();
        prescription3.setUserId(2L);
        prescription3.setFileName("prescription3.pdf");
        prescription3.setFileUrl("uploads/prescription3.pdf");
        prescription3.setStatus(PrescriptionStatus.PENDING);

        prescriptionRepository.save(prescription1);
        prescriptionRepository.save(prescription2);
        prescriptionRepository.save(prescription3);

        // When
        List<Prescription> userPrescriptions = prescriptionRepository.findByUserId(1L);

        // Then
        assertThat(userPrescriptions).hasSize(2);
        assertThat(userPrescriptions.stream().allMatch(p -> p.getUserId().equals(1L))).isTrue();
    }

    @Test
    void findById_ShouldReturnPrescription() {
        // Given
        Prescription prescription = new Prescription();
        prescription.setUserId(1L);
        prescription.setFileName("test.pdf");
        prescription.setFileUrl("uploads/test.pdf");
        prescription.setStatus(PrescriptionStatus.PENDING);

        Prescription saved = prescriptionRepository.save(prescription);

        // When
        var found = prescriptionRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFileName()).isEqualTo("test.pdf");
    }

    @Test
    void save_ShouldPersistPrescription() {
        // Given
        Prescription prescription = new Prescription();
        prescription.setUserId(1L);
        prescription.setFileName("new.pdf");
        prescription.setFileUrl("uploads/new.pdf");
        prescription.setStatus(PrescriptionStatus.PENDING);

        // When
        Prescription saved = prescriptionRepository.save(prescription);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFileName()).isEqualTo("new.pdf");
    }

    @Test
    void findByOrderId_ShouldReturnPrescription() {
        Prescription prescription = new Prescription();
        prescription.setUserId(1L);
        prescription.setOrderId(77L);
        prescription.setFileName("linked.pdf");
        prescription.setFileUrl("uploads/linked.pdf");
        prescription.setStatus(PrescriptionStatus.PENDING);

        Prescription saved = prescriptionRepository.save(prescription);

        var found = prescriptionRepository.findByOrderId(77L);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getOrderId()).isEqualTo(77L);
    }
}
