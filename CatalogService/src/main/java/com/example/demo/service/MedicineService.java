package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.dto.MedicineDTO;
import com.example.demo.dto.MedicineRequest;
import com.example.demo.dto.PagedResponseDTO;
import com.example.demo.entitty.Category;
import com.example.demo.entitty.Medicine;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.MedicineRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicineService {

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @Cacheable(value = "medicines")
    public List<MedicineDTO> getAllMedicines() {
        return medicineRepository.findByIsActiveTrue()
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Cacheable(value = "medicine", key = "#id")
    public MedicineDTO getMedicineById(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found: " + id));
        return mapToDTO(medicine);
    }

    public List<MedicineDTO> searchMedicines(String name, Long categoryId,
                                              Boolean requiresPrescription) {
        return medicineRepository.searchMedicines(name, categoryId, requiresPrescription)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public PagedResponseDTO<MedicineDTO> getMedicinesPage(
            String name,
            Long categoryId,
            Boolean requiresPrescription,
            int page,
            int size,
            String sortBy,
            String sortDir) {
        Sort sort = Sort.by("desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Medicine> medicinesPage;

        if (name != null || categoryId != null || requiresPrescription != null) {
            medicinesPage = medicineRepository.searchMedicines(name, categoryId, requiresPrescription, pageable);
        } else {
            medicinesPage = medicineRepository.findByIsActiveTrue(pageable);
        }

        PagedResponseDTO<MedicineDTO> response = new PagedResponseDTO<>();
        response.setContent(medicinesPage.getContent().stream().map(this::mapToDTO).collect(Collectors.toList()));
        response.setPage(medicinesPage.getNumber());
        response.setSize(medicinesPage.getSize());
        response.setTotalElements(medicinesPage.getTotalElements());
        response.setTotalPages(medicinesPage.getTotalPages());
        response.setFirst(medicinesPage.isFirst());
        response.setLast(medicinesPage.isLast());
        return response;
    }

    @CacheEvict(value = "medicines", allEntries = true)
    @Cacheable(value = "medicine", key = "#result.id")
    public MedicineDTO createMedicine(MedicineRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + request.getCategoryId()));

        // No builder — use new + setters
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

        return mapToDTO(medicineRepository.save(medicine));
    }

    @Caching(evict = {
        @CacheEvict(value = "medicines", allEntries = true),
        @CacheEvict(value = "medicine", key = "#id")
    })
    public MedicineDTO updateMedicine(Long id, MedicineRequest request) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + request.getCategoryId()));

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

        return mapToDTO(medicineRepository.save(medicine));
    }

    @Caching(evict = {
        @CacheEvict(value = "medicines", allEntries = true),
        @CacheEvict(value = "medicine", key = "#id")
    })
    public void deleteMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found: " + id));
        medicine.setActive(false);
        medicineRepository.save(medicine);
    }

    public MedicineDTO mapToDTO(Medicine m) {
        // No builder — use new + setters
        MedicineDTO dto = new MedicineDTO();
        dto.setId(m.getId());
        dto.setName(m.getName());
        dto.setDescription(m.getDescription());
        dto.setCategory(categoryService.mapToDTO(m.getCategory()));
        dto.setManufacturer(m.getManufacturer());
        dto.setPrice(m.getPrice());
        dto.setStock(m.getStock());
        dto.setRequiresPrescription(m.isRequiresPrescription());
        dto.setImageUrl(m.getImageUrl());
        dto.setDosageInfo(m.getDosageInfo());
        dto.setSideEffects(m.getSideEffects());
        dto.setActive(m.isActive());
        return dto;
    }
}
