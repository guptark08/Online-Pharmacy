package com.pharmacy.admin.repository;

import com.pharmacy.admin.entity.Prescription;
import com.pharmacy.admin.enums.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByStatus(PrescriptionStatus status);

    List<Prescription> findByOrderByUploadedAtDesc();

    Optional<Prescription> findByOrderId(Long orderId);
}
