package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entitty.Prescription;
import com.example.demo.enums.PrescriptionStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByUserId(Long userId);
    List<Prescription> findByStatus(PrescriptionStatus status);
    List<Prescription> findByUserIdAndStatus(Long userId, PrescriptionStatus status);
    Optional<Prescription> findByOrderId(Long orderId);
}
