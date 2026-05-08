package com.pharmacy.admin.service;

import com.pharmacy.admin.dto.MedicineRequest;
import com.pharmacy.admin.entity.Category;
import com.pharmacy.admin.entity.InventoryBatch;
import com.pharmacy.admin.entity.Medicine;
import com.pharmacy.admin.dto.BatchRequest;
import com.pharmacy.admin.repository.CategoryRepository;
import com.pharmacy.admin.repository.InventoryBatchRepository;
import com.pharmacy.admin.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminMedicineService {

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private InventoryBatchRepository inventoryBatchRepository;

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findByIsActiveTrue();
    }

    public Medicine getMedicineById(Long id) {
        return medicineRepository.findById(id)
            .orElseThrow(() ->
                new RuntimeException("Medicine not found: " + id));
    }

    public Medicine createMedicine(MedicineRequest request) {
        Category category = categoryRepository.findById(
                request.getCategoryId())
            .orElseThrow(() ->
                new RuntimeException("Category not found"));

        Medicine medicine = new Medicine();
        medicine.setName(request.getName());
        medicine.setDescription(request.getDescription());
        medicine.setCategory(category);
        medicine.setManufacturer(request.getManufacturer());
        medicine.setPrice(request.getPrice());
        medicine.setStock(request.getStock());
        medicine.setRequiresPrescription(request.isRequiresPrescription());
        medicine.setImageUrl(request.getImageUrl());
        medicine.setDosageInfo(request.getDosageInfo());
        medicine.setSideEffects(request.getSideEffects());
        medicine.setActive(true);

        return medicineRepository.save(medicine);
    }

    public Medicine updateMedicine(Long id, MedicineRequest request) {
        Medicine medicine = medicineRepository.findById(id)
            .orElseThrow(() ->
                new RuntimeException("Medicine not found: " + id));

        Category category = categoryRepository.findById(
                request.getCategoryId())
            .orElseThrow(() ->
                new RuntimeException("Category not found"));

        medicine.setName(request.getName());
        medicine.setDescription(request.getDescription());
        medicine.setCategory(category);
        medicine.setManufacturer(request.getManufacturer());
        medicine.setPrice(request.getPrice());
        medicine.setStock(request.getStock());
        medicine.setRequiresPrescription(request.isRequiresPrescription());
        medicine.setImageUrl(request.getImageUrl());
        medicine.setDosageInfo(request.getDosageInfo());
        medicine.setSideEffects(request.getSideEffects());

        return medicineRepository.save(medicine);
    }

    public void deleteMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id)
            .orElseThrow(() ->
                new RuntimeException("Medicine not found: " + id));
        medicine.setActive(false);
        medicineRepository.save(medicine);
    }

    public InventoryBatch addBatch(Long medicineId,
                                    BatchRequest request) {
        Medicine medicine = medicineRepository.findById(medicineId)
            .orElseThrow(() ->
                new RuntimeException("Medicine not found: " + medicineId));

        InventoryBatch batch = new InventoryBatch();
        batch.setMedicine(medicine);
        batch.setBatchNumber(request.getBatchNumber());
        batch.setQuantity(request.getQuantity());
        batch.setExpiryDate(request.getExpiryDate());
        batch.setManufactureDate(request.getManufactureDate());
        batch.setAddedAt(LocalDateTime.now());

        // Update medicine stock
        medicine.setStock(medicine.getStock() + request.getQuantity());
        medicineRepository.save(medicine);

        return inventoryBatchRepository.save(batch);
    }

    public List<InventoryBatch> getBatches(Long medicineId) {
        return inventoryBatchRepository.findByMedicineId(medicineId);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category createCategory(Category request) {
        return categoryRepository.save(request);
    }
}
