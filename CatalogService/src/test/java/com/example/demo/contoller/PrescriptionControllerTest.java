package com.example.demo.contoller;

import com.example.demo.contoller.PrescriptionController;
import com.example.demo.dto.PrescriptionResponseDTO;
import com.example.demo.enums.PrescriptionStatus;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.PrescriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionControllerTest {

    @Mock
    private PrescriptionService prescriptionService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private PrescriptionController prescriptionController;

    private PrescriptionResponseDTO testResponseDTO;
    private MultipartFile testFile;

    @BeforeEach
    void setUp() {
        testResponseDTO = new PrescriptionResponseDTO();
        testResponseDTO.setId(1L);
        testResponseDTO.setFileName("prescription.pdf");
        testResponseDTO.setStatus(PrescriptionStatus.PENDING);

        testFile = new MockMultipartFile("file", "prescription.pdf", "application/pdf", "content".getBytes());
    }

    @Test
    void uploadPrescription() throws IOException {
        when(jwtUtil.extractUsername("token")).thenReturn("user@example.com");
        when(prescriptionService.uploadPrescription(any(MultipartFile.class), any(Long.class))).thenReturn(testResponseDTO);

        ResponseEntity<PrescriptionResponseDTO> response = prescriptionController.uploadPrescription(testFile, "Bearer token");

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("prescription.pdf", response.getBody().getFileName());
        verify(prescriptionService).uploadPrescription(any(MultipartFile.class), any(Long.class));
    }

    @Test
    void getById() {
        when(prescriptionService.getPrescriptionById(1L)).thenReturn(testResponseDTO);

        ResponseEntity<PrescriptionResponseDTO> response = prescriptionController.getById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(prescriptionService).getPrescriptionById(1L);
    }

    @Test
    void getByOrderId() {
        testResponseDTO.setOrderId(55L);
        when(prescriptionService.getPrescriptionByOrderId(55L)).thenReturn(testResponseDTO);

        ResponseEntity<PrescriptionResponseDTO> response =
            prescriptionController.getByOrderId(55L);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(55L, response.getBody().getOrderId());
        verify(prescriptionService).getPrescriptionByOrderId(55L);
    }

    @Test
    void getMyPrescriptions() {
        when(jwtUtil.extractUsername("token")).thenReturn("user@example.com");
        when(prescriptionService.getMyPrescriptions(any(Long.class))).thenReturn(List.of(testResponseDTO));

        ResponseEntity<List<PrescriptionResponseDTO>> response = prescriptionController.getMyPrescriptions("Bearer token");

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(prescriptionService).getMyPrescriptions(any(Long.class));
    }

    @Test
    void assignOrder() {
        testResponseDTO.setOrderId(77L);
        when(prescriptionService.assignOrder(1L, 77L)).thenReturn(testResponseDTO);

        ResponseEntity<PrescriptionResponseDTO> response =
            prescriptionController.assignOrder(1L, 77L);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(77L, response.getBody().getOrderId());
        verify(prescriptionService).assignOrder(1L, 77L);
    }
}
