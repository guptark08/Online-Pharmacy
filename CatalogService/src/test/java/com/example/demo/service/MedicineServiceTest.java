package com.example.demo.service;

import com.example.demo.dto.MedicineDTO;
import com.example.demo.dto.MedicineRequest;
import com.example.demo.entitty.Category;
import com.example.demo.entitty.Medicine;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.MedicineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicineServiceTest {

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private MedicineService medicineService;

    private Category testCategory;
    private Medicine testMedicine;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Pain Relief");
        testCategory.setDescription("Pain relief medications");

        testMedicine = new Medicine();
        testMedicine.setId(1L);
        testMedicine.setName("Aspirin");
        testMedicine.setDescription("Pain reliever");
        testMedicine.setCategory(testCategory);
        testMedicine.setManufacturer("Bayer");
        testMedicine.setPrice(BigDecimal.valueOf(10.00));
        testMedicine.setStock(100);
        testMedicine.setRequiresPrescription(false);
        testMedicine.setActive(true);
    }

    @Test
    void getAllMedicines_Success() {
        when(medicineRepository.findByIsActiveTrue()).thenReturn(List.of(testMedicine));

        List<MedicineDTO> medicines = medicineService.getAllMedicines();

        assertNotNull(medicines);
        assertEquals(1, medicines.size());
        assertEquals("Aspirin", medicines.get(0).getName());
        verify(medicineRepository).findByIsActiveTrue();
    }

    @Test
    void getAllMedicines_Empty() {
        when(medicineRepository.findByIsActiveTrue()).thenReturn(List.of());

        List<MedicineDTO> medicines = medicineService.getAllMedicines();

        assertNotNull(medicines);
        assertEquals(0, medicines.size());
    }

    @Test
    void getMedicineById_Success() {
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(testMedicine));

        MedicineDTO medicine = medicineService.getMedicineById(1L);

        assertNotNull(medicine);
        assertEquals(1L, medicine.getId());
        assertEquals("Aspirin", medicine.getName());
    }

    @Test
    void getMedicineById_NotFound() {
        when(medicineRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> medicineService.getMedicineById(999L));

        assertTrue(exception.getMessage().contains("Medicine not found"));
    }

    @Test
    void searchMedicines_Success() {
        when(medicineRepository.searchMedicines("Aspir", null, null)).thenReturn(List.of(testMedicine));

        List<MedicineDTO> medicines = medicineService.searchMedicines("Aspir", null, null);

        assertNotNull(medicines);
        assertEquals(1, medicines.size());
        assertEquals("Aspirin", medicines.get(0).getName());
    }

    @Test
    void createMedicine_Success() {
        MedicineRequest request = new MedicineRequest();
        request.setName("Ibuprofen");
        request.setDescription("Pain reliever");
        request.setCategoryId(1L);
        request.setManufacturer("Advil");
        request.setPrice(BigDecimal.valueOf(15.00));
        request.setStock(50);
        request.setRequiresPrescription(false);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(medicineRepository.save(any(Medicine.class))).thenAnswer(invocation -> {
            Medicine m = invocation.getArgument(0);
            m.setId(2L);
            return m;
        });

        MedicineDTO result = medicineService.createMedicine(request);

        assertNotNull(result);
        assertEquals("Ibuprofen", result.getName());
        verify(medicineRepository).save(any(Medicine.class));
    }

    @Test
    void createMedicine_CategoryNotFound() {
        MedicineRequest request = new MedicineRequest();
        request.setName("Ibuprofen");
        request.setCategoryId(999L);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> medicineService.createMedicine(request));

        assertTrue(exception.getMessage().contains("Category not found"));
    }

    @Test
    void updateMedicine_Success() {
        MedicineRequest request = new MedicineRequest();
        request.setName("Aspirin Updated");
        request.setDescription("Updated description");
        request.setCategoryId(1L);
        request.setManufacturer("Bayer AG");
        request.setPrice(BigDecimal.valueOf(12.00));
        request.setStock(150);
        request.setRequiresPrescription(false);

        when(medicineRepository.findById(1L)).thenReturn(Optional.of(testMedicine));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(medicineRepository.save(any(Medicine.class))).thenReturn(testMedicine);

        MedicineDTO result = medicineService.updateMedicine(1L, request);

        assertNotNull(result);
        verify(medicineRepository).save(any(Medicine.class));
    }

    @Test
    void updateMedicine_NotFound() {
        MedicineRequest request = new MedicineRequest();
        request.setName("Test");

        when(medicineRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> medicineService.updateMedicine(999L, request));

        assertTrue(exception.getMessage().contains("Medicine not found"));
    }

    @Test
    void deleteMedicine_Success() {
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(testMedicine));
        when(medicineRepository.save(any(Medicine.class))).thenReturn(testMedicine);

        medicineService.deleteMedicine(1L);

        verify(medicineRepository).save(any(Medicine.class));
        assertFalse(testMedicine.isActive());
    }

    @Test
    void deleteMedicine_NotFound() {
        when(medicineRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> medicineService.deleteMedicine(999L));

        assertTrue(exception.getMessage().contains("Medicine not found"));
    }

    @Test
    void mapToDTO_Success() {
        MedicineDTO dto = medicineService.mapToDTO(testMedicine);

        assertNotNull(dto);
        assertEquals(testMedicine.getId(), dto.getId());
        assertEquals(testMedicine.getName(), dto.getName());
        assertEquals(testMedicine.getDescription(), dto.getDescription());
        assertEquals(testMedicine.getManufacturer(), dto.getManufacturer());
        assertEquals(testMedicine.getPrice(), dto.getPrice());
        assertEquals(testMedicine.getStock(), dto.getStock());
        assertTrue(dto.isActive());
    }
}
