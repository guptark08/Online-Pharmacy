package com.example.demo.service;

import com.example.demo.dto.PrescriptionResponseDTO;
import com.example.demo.entitty.Prescription;
import com.example.demo.enums.PrescriptionStatus;
import com.example.demo.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public static class PrescriptionFileData {
        private final Path path;
        private final String fileName;
        private final String contentType;

        public PrescriptionFileData(Path path, String fileName, String contentType) {
            this.path = path;
            this.fileName = fileName;
            this.contentType = contentType;
        }

        public Path getPath() {
            return path;
        }

        public String getFileName() {
            return fileName;
        }

        public String getContentType() {
            return contentType;
        }
    }

    public PrescriptionResponseDTO uploadPrescription(MultipartFile file, Long userId) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null
            || (!contentType.equals("image/jpeg")
                && !contentType.equals("image/png")
                && !contentType.equals("application/pdf"))) {
            throw new RuntimeException("Only JPG, PNG, and PDF files are allowed");
        }

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(uniqueFileName).normalize();
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Prescription prescription = new Prescription();
        prescription.setUserId(userId);
        prescription.setFileUrl("/uploads/" + uniqueFileName);
        prescription.setFileName(file.getOriginalFilename());
        prescription.setFileType(contentType);
        prescription.setStatus(PrescriptionStatus.PENDING);

        return mapToDTO(prescriptionRepository.save(prescription));
    }

    public PrescriptionResponseDTO getPrescriptionById(Long id) {
        Prescription prescription = getPrescriptionEntity(id);
        return mapToDTO(prescription);
    }

    public PrescriptionResponseDTO getPrescriptionByOrderId(Long orderId) {
        Prescription prescription = prescriptionRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Prescription not found for order: " + orderId));
        return mapToDTO(prescription);
    }

    public PrescriptionResponseDTO assignOrder(Long prescriptionId, Long orderId) {
        Prescription prescription = getPrescriptionEntity(prescriptionId);

        prescription.setOrderId(orderId);
        return mapToDTO(prescriptionRepository.save(prescription));
    }

    public List<PrescriptionResponseDTO> getMyPrescriptions(Long userId) {
        return prescriptionRepository.findByUserId(userId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    public PrescriptionFileData getPrescriptionFile(Long id) throws IOException {
        Prescription prescription = getPrescriptionEntity(id);
        Path filePath = resolveStoredFilePath(prescription.getFileUrl());

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new RuntimeException("Prescription file not found for id: " + id);
        }

        String contentType = prescription.getFileType();
        if (contentType == null || contentType.isBlank()) {
            contentType = Files.probeContentType(filePath);
        }
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        String fileName = prescription.getFileName();
        if (fileName == null || fileName.isBlank()) {
            fileName = Objects.requireNonNullElse(filePath.getFileName(), filePath).toString();
        }

        return new PrescriptionFileData(filePath, fileName, contentType);
    }

    private Prescription getPrescriptionEntity(Long id) {
        return prescriptionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Prescription not found: " + id));
    }

    private Path resolveStoredFilePath(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            throw new RuntimeException("Prescription file path is missing");
        }

        Path directPath = Paths.get(storedPath).normalize();
        if (directPath.isAbsolute() && Files.exists(directPath)) {
            return directPath;
        }

        Path uploadBasePath = Paths.get(uploadDir).toAbsolutePath().normalize();

        if (!directPath.isAbsolute()) {
            Path relativeCandidate = uploadBasePath.resolve(directPath).normalize();
            if (Files.exists(relativeCandidate)) {
                return relativeCandidate;
            }
        }

        String normalized = storedPath.replace("\\", "/");
        String uploadRelativePath = normalized.replaceFirst("^/?uploads/?", "");
        Path uploadCandidate = uploadBasePath.resolve(uploadRelativePath).normalize();
        if (Files.exists(uploadCandidate)) {
            return uploadCandidate;
        }

        int uploadsIndex = normalized.indexOf("/uploads/");
        if (uploadsIndex >= 0) {
            String fromUploadsSegment = normalized.substring(uploadsIndex + "/uploads/".length());
            Path segmentCandidate = uploadBasePath.resolve(fromUploadsSegment).normalize();
            if (Files.exists(segmentCandidate)) {
                return segmentCandidate;
            }
        }

        Path fileName = Paths.get(normalized).getFileName();
        if (fileName != null) {
            Path fileNameCandidate = uploadBasePath.resolve(fileName).normalize();
            if (Files.exists(fileNameCandidate)) {
                return fileNameCandidate;
            }
        }

        throw new RuntimeException("Prescription file not found: " + storedPath);
    }

    private PrescriptionResponseDTO mapToDTO(Prescription prescription) {
        PrescriptionResponseDTO dto = new PrescriptionResponseDTO();
        dto.setId(prescription.getId());
        dto.setUserId(prescription.getUserId());
        dto.setOrderId(prescription.getOrderId());
        dto.setFileName(prescription.getFileName());
        dto.setFileUrl(toPublicFileUrl(prescription.getFileUrl()));
        dto.setFileType(prescription.getFileType());
        dto.setStatus(prescription.getStatus());
        dto.setUploadedAt(prescription.getUploadedAt());
        dto.setReviewedAt(prescription.getReviewedAt());
        dto.setReviewNote(prescription.getReviewNote());
        return dto;
    }

    private String toPublicFileUrl(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return null;
        }

        String normalized = storedPath.replace("\\", "/");
        if (normalized.startsWith("/uploads/")) {
            return normalized;
        }

        int uploadsIndex = normalized.indexOf("/uploads/");
        if (uploadsIndex >= 0) {
            return normalized.substring(uploadsIndex);
        }

        Path fileName = Paths.get(normalized).getFileName();
        if (fileName == null) {
            return null;
        }

        return "/uploads/" + fileName;
    }
}
