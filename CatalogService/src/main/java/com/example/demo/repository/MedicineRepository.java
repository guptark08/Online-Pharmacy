package com.example.demo.repository;

import com.example.demo.entitty.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findByIsActiveTrue();
    Page<Medicine> findByIsActiveTrue(Pageable pageable);

    List<Medicine> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);

    List<Medicine> findByCategoryIdAndIsActiveTrue(Long categoryId);

    List<Medicine> findByRequiresPrescriptionAndIsActiveTrue(boolean requiresPrescription);

    List<Medicine> findByStockLessThanAndIsActiveTrue(int threshold);

    @Query("SELECT m FROM Medicine m WHERE " +
           "(:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:categoryId IS NULL OR m.category.id = :categoryId) AND " +
           "(:requiresPrescription IS NULL OR m.requiresPrescription = :requiresPrescription) AND " +
           "m.isActive = true")
    List<Medicine> searchMedicines(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("requiresPrescription") Boolean requiresPrescription);
k
    @Query("SELECT m FROM Medicine m WHERE " +
           "(:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:categoryId IS NULL OR m.category.id = :categoryId) AND " +
           "(:requiresPrescription IS NULL OR m.requiresPrescription = :requiresPrescription) AND " +
           "m.isActive = true")
    Page<Medicine> searchMedicines(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("requiresPrescription") Boolean requiresPrescription,
            Pageable pageable);
}
