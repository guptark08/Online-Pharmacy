package com.example.demo.contoller;

import com.example.demo.contoller.MedicineController;
import com.example.demo.dto.MedicineDTO;
import com.example.demo.dto.MedicineRequest;
import com.example.demo.dto.PagedResponseDTO;
import com.example.demo.service.MedicineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicineControllerTest {

    @Mock
    private MedicineService medicineService;

    @InjectMocks
    private MedicineController medicineController;

    private MedicineDTO testMedicineDTO;
    private MedicineRequest testRequest;

    @BeforeEach
    void setUp() {
        testMedicineDTO = new MedicineDTO();
        testMedicineDTO.setId(1L);
        testMedicineDTO.setName("Aspirin");
        testMedicineDTO.setDescription("Pain reliever");
        testMedicineDTO.setPrice(BigDecimal.valueOf(10.00));
        testMedicineDTO.setStock(100);
        testMedicineDTO.setRequiresPrescription(false);

        testRequest = new MedicineRequest();
        testRequest.setName("Aspirin");
        testRequest.setDescription("Pain reliever");
        testRequest.setCategoryId(1L);
        testRequest.setManufacturer("Bayer");
        testRequest.setPrice(BigDecimal.valueOf(10.00));
        testRequest.setStock(100);
        testRequest.setRequiresPrescription(false);
    }

    @Test
    void getMedicines_All() {
        PagedResponseDTO<MedicineDTO> page = new PagedResponseDTO<>();
        page.setContent(java.util.List.of(testMedicineDTO));
        page.setTotalElements(1);
        when(medicineService.getMedicinesPage(null, null, null, 0, 9, "name", "asc")).thenReturn(page);

        ResponseEntity<PagedResponseDTO<MedicineDTO>> response = medicineController.getMedicines(
                null, null, null, 0, 9, "name", "asc");

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        verify(medicineService).getMedicinesPage(null, null, null, 0, 9, "name", "asc");
    }

    @Test
    void getMedicines_Search() {
        PagedResponseDTO<MedicineDTO> page = new PagedResponseDTO<>();
        page.setContent(java.util.List.of(testMedicineDTO));
        when(medicineService.getMedicinesPage("Aspirin", null, null, 0, 9, "name", "asc")).thenReturn(page);

        ResponseEntity<PagedResponseDTO<MedicineDTO>> response = medicineController.getMedicines(
                "Aspirin", null, null, 0, 9, "name", "asc");

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        verify(medicineService).getMedicinesPage("Aspirin", null, null, 0, 9, "name", "asc");
    }

    @Test
    void getMedicineById() {
        when(medicineService.getMedicineById(1L)).thenReturn(testMedicineDTO);

        ResponseEntity<MedicineDTO> response = medicineController.getMedicineById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Aspirin", response.getBody().getName());
        verify(medicineService).getMedicineById(1L);
    }

    @Test
    void createMedicine() {
        when(medicineService.createMedicine(any(MedicineRequest.class))).thenReturn(testMedicineDTO);

        ResponseEntity<MedicineDTO> response = medicineController.createMedicine(testRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Aspirin", response.getBody().getName());
        verify(medicineService).createMedicine(any(MedicineRequest.class));
    }

    @Test
    void updateMedicine() {
        when(medicineService.updateMedicine(1L, testRequest)).thenReturn(testMedicineDTO);

        ResponseEntity<MedicineDTO> response = medicineController.updateMedicine(1L, testRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Aspirin", response.getBody().getName());
        verify(medicineService).updateMedicine(1L, testRequest);
    }

    @Test
    void deleteMedicine() {
        doNothing().when(medicineService).deleteMedicine(1L);

        ResponseEntity<String> response = medicineController.deleteMedicine(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Medicine deleted successfully", response.getBody());
        verify(medicineService).deleteMedicine(1L);
    }
}
