package com.pharmacy.admin.service;

import com.pharmacy.admin.dto.MedicineRequest;
import com.pharmacy.admin.dto.BatchRequest;
import com.pharmacy.admin.entity.Category;
import com.pharmacy.admin.entity.InventoryBatch;
import com.pharmacy.admin.entity.Medicine;
import com.pharmacy.admin.repository.CategoryRepository;
import com.pharmacy.admin.repository.InventoryBatchRepository;
import com.pharmacy.admin.repository.MedicineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminMedicineServiceTest {

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private InventoryBatchRepository inventoryBatchRepository;

    @InjectMocks
    private AdminMedicineService adminMedicineService;

    private Category testCategory;
    private Medicine testMedicine;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Pain Relief");

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

        List<Medicine> medicines = adminMedicineService.getAllMedicines();

        assertNotNull(medicines);
        assertEquals(1, medicines.size());
        assertEquals("Aspirin", medicines.get(0).getName());
    }

    @Test
    void getMedicineById_Success() {
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(testMedicine));

        Medicine medicine = adminMedicineService.getMedicineById(1L);

        assertNotNull(medicine);
        assertEquals("Aspirin", medicine.getName());
    }

    @Test
    void getMedicineById_NotFound() {
        when(medicineRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminMedicineService.getMedicineById(999L));

        assertTrue(exception.getMessage().contains("Medicine not found"));
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

        Medicine result = adminMedicineService.createMedicine(request);

        assertNotNull(result);
        assertEquals("Ibuprofen", result.getName());
        assertTrue(result.isActive());
    }

    @Test
    void createMedicine_CategoryNotFound() {
        MedicineRequest request = new MedicineRequest();
        request.setName("Test");
        request.setCategoryId(999L);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminMedicineService.createMedicine(request));

        assertTrue(exception.getMessage().contains("Category not found"));
    }

    @Test
    void updateMedicine_Success() {
        MedicineRequest request = new MedicineRequest();
        request.setName("Aspirin Updated");
        request.setDescription("Updated");
        request.setCategoryId(1L);
        request.setManufacturer("Bayer AG");
        request.setPrice(BigDecimal.valueOf(12.00));
        request.setStock(150);
        request.setRequiresPrescription(false);

        when(medicineRepository.findById(1L)).thenReturn(Optional.of(testMedicine));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(medicineRepository.save(any(Medicine.class))).thenReturn(testMedicine);

        Medicine result = adminMedicineService.updateMedicine(1L, request);

        assertNotNull(result);
        verify(medicineRepository).save(any(Medicine.class));
    }

    @Test
    void updateMedicine_NotFound() {
        MedicineRequest request = new MedicineRequest();
        request.setName("Test");

        when(medicineRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminMedicineService.updateMedicine(999L, request));

        assertTrue(exception.getMessage().contains("Medicine not found"));
    }

    @Test
    void deleteMedicine_Success() {
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(testMedicine));
        when(medicineRepository.save(any(Medicine.class))).thenReturn(testMedicine);

        adminMedicineService.deleteMedicine(1L);

        verify(medicineRepository).save(any(Medicine.class));
        assertFalse(testMedicine.isActive());
    }

    @Test
    void deleteMedicine_NotFound() {
        when(medicineRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminMedicineService.deleteMedicine(999L));

        assertTrue(exception.getMessage().contains("Medicine not found"));
    }

    @Test
    void addBatch_Success() {
        BatchRequest request = new BatchRequest();
        request.setBatchNumber("BATCH001");
        request.setQuantity(50);
        request.setExpiryDate(LocalDate.now().plusYears(2));
        request.setManufactureDate(LocalDate.now());

        when(medicineRepository.findById(1L)).thenReturn(Optional.of(testMedicine));
        when(medicineRepository.save(any(Medicine.class))).thenReturn(testMedicine);
        when(inventoryBatchRepository.save(any(InventoryBatch.class))).thenAnswer(invocation -> {
            InventoryBatch batch = invocation.getArgument(0);
            batch.setId(1L);
            return batch;
        });

        InventoryBatch result = adminMedicineService.addBatch(1L, request);

        assertNotNull(result);
        assertEquals("BATCH001", result.getBatchNumber());
        verify(medicineRepository).save(any(Medicine.class));
    }

    @Test
    void addBatch_MedicineNotFound() {
        BatchRequest request = new BatchRequest();

        when(medicineRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminMedicineService.addBatch(999L, request));

        assertTrue(exception.getMessage().contains("Medicine not found"));
    }

    @Test
    void getBatches_Success() {
        InventoryBatch batch = new InventoryBatch();
        batch.setId(1L);
        batch.setMedicine(testMedicine);
        batch.setBatchNumber("BATCH001");

        when(inventoryBatchRepository.findByMedicineId(1L)).thenReturn(List.of(batch));

        List<InventoryBatch> batches = adminMedicineService.getBatches(1L);

        assertNotNull(batches);
        assertEquals(1, batches.size());
        assertEquals("BATCH001", batches.get(0).getBatchNumber());
    }

    @Test
    void getAllCategories_Success() {
        when(categoryRepository.findAll()).thenReturn(List.of(testCategory));

        List<Category> categories = adminMedicineService.getAllCategories();

        assertNotNull(categories);
        assertEquals(1, categories.size());
    }

    @Test
    void createCategory_Success() {
        Category newCategory = new Category();
        newCategory.setName("New Category");

        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            c.setId(2L);
            return c;
        });

        Category result = adminMedicineService.createCategory(newCategory);

        assertNotNull(result);
        assertEquals("New Category", result.getName());
    }
}
