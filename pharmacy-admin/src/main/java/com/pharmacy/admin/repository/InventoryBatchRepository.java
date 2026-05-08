package com.pharmacy.admin.repository;

import com.pharmacy.admin.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {

    List<InventoryBatch> findByMedicineId(Long medicineId);
}
