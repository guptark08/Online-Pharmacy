package com.example.demo.service;

import com.example.demo.dto.PrescriptionResponseDTO;
import com.example.demo.entitty.Prescription;
import com.example.demo.enums.PrescriptionStatus;
import com.example.demo.repository.PrescriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @InjectMocks
    private PrescriptionService prescriptionService;

    private Prescription testPrescription;
    private MultipartFile testFile;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(prescriptionService, "uploadDir", "uploads");

        testPrescription = new Prescription();
        testPrescription.setId(1L);
        testPrescription.setUserId(1L);
        testPrescription.setFileName("prescription.pdf");
        testPrescription.setFileUrl("uploads/uuid_prescription.pdf");
        testPrescription.setFileType("application/pdf");
        testPrescription.setStatus(PrescriptionStatus.PENDING);

        testFile = new MockMultipartFile("file", "prescription.pdf", "application/pdf", "content".getBytes());
    }

    @Test
    void uploadPrescription_Success() throws IOException {
        UUID testUUID = UUID.fromString("12345678-1234-1234-1234-123456789012");
        
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class);
             MockedStatic<Paths> mockedPaths = mockStatic(Paths.class);
             MockedStatic<UUID> mockedUUID = mockStatic(UUID.class)) {

            Path mockPath = mock(Path.class);
            Path mockFilePath = mock(Path.class);

            mockedPaths.when(() -> Paths.get("uploads")).thenReturn(mockPath);
            mockedFiles.when(() -> Files.exists(mockPath)).thenReturn(true);
            mockedUUID.when(UUID::randomUUID).thenReturn(testUUID);
            mockedPaths.when(() -> mockPath.resolve(any(String.class))).thenReturn(mockFilePath);
            mockedFiles.when(() -> Files.copy(any(java.io.InputStream.class), eq(mockFilePath), any())).thenReturn(0L);

            when(prescriptionRepository.save(any(Prescription.class))).thenReturn(testPrescription);

            PrescriptionResponseDTO response = prescriptionService.uploadPrescription(testFile, 1L);

            assertNotNull(response);
            assertEquals("prescription.pdf", response.getFileName());
            assertEquals(PrescriptionStatus.PENDING, response.getStatus());
            verify(prescriptionRepository).save(any(Prescription.class));
        }
    }

    @Test
    void uploadPrescription_InvalidFileType() {
        MultipartFile invalidFile = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> prescriptionService.uploadPrescription(invalidFile, 1L));

        assertEquals("Only JPG, PNG, and PDF files are allowed", exception.getMessage());
    }

    @Test
    void getPrescriptionById_Success() {
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(testPrescription));

        PrescriptionResponseDTO response = prescriptionService.getPrescriptionById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("prescription.pdf", response.getFileName());
        verify(prescriptionRepository).findById(1L);
    }

    @Test
    void getPrescriptionById_NotFound() {
        when(prescriptionRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> prescriptionService.getPrescriptionById(999L));

        assertEquals("Prescription not found: 999", exception.getMessage());
        verify(prescriptionRepository).findById(999L);
    }

    @Test
    void getPrescriptionByOrderId_Success() {
        testPrescription.setOrderId(55L);
        when(prescriptionRepository.findByOrderId(55L))
            .thenReturn(Optional.of(testPrescription));

        PrescriptionResponseDTO response =
            prescriptionService.getPrescriptionByOrderId(55L);

        assertNotNull(response);
        assertEquals(55L, response.getOrderId());
        verify(prescriptionRepository).findByOrderId(55L);
    }

    @Test
    void assignOrder_Success() {
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(testPrescription));
        when(prescriptionRepository.save(any(Prescription.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        PrescriptionResponseDTO response = prescriptionService.assignOrder(1L, 99L);

        assertNotNull(response);
        assertEquals(99L, response.getOrderId());
        verify(prescriptionRepository).save(testPrescription);
    }

    @Test
    void getMyPrescriptions() {
        when(prescriptionRepository.findByUserId(1L)).thenReturn(List.of(testPrescription));

        List<PrescriptionResponseDTO> responses = prescriptionService.getMyPrescriptions(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("prescription.pdf", responses.get(0).getFileName());
        verify(prescriptionRepository).findByUserId(1L);
    }
}
