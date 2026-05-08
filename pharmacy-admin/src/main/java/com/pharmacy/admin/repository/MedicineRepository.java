package com.pharmacy.admin.repository;

import com.pharmacy.admin.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByIsActiveTrue();
    List<Medicine> findByStockLessThanAndIsActiveTrue(int threshold);
    List<Medicine> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
}