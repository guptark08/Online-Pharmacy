package com.example.demo.contoller;

import com.example.demo.dto.PrescriptionResponseDTO;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/catalog/prescriptions")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(
            summary = "Upload a prescription file",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = PrescriptionUploadRequest.class)
                    )
            )
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PrescriptionResponseDTO> uploadPrescription(
            @RequestPart("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader) throws IOException {

        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        // Use email hashcode as unique userId
        Long userId = Integer.toUnsignedLong(email.hashCode());

        return ResponseEntity.ok(
            prescriptionService.uploadPrescription(file, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionResponseDTO> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            prescriptionService.getPrescriptionById(id));
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getPrescriptionFile(
            @PathVariable Long id) throws IOException {
        PrescriptionService.PrescriptionFileData fileData =
            prescriptionService.getPrescriptionFile(id);
        Resource resource = new UrlResource(fileData.getPath().toUri());

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(fileData.getContentType());
        } catch (Exception ignored) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        ContentDisposition disposition = ContentDisposition
            .inline()
            .filename(fileData.getFileName(), StandardCharsets.UTF_8)
            .build();

        return ResponseEntity.ok()
            .contentType(mediaType)
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .body(resource);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PrescriptionResponseDTO> getByOrderId(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(
            prescriptionService.getPrescriptionByOrderId(orderId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<PrescriptionResponseDTO>> getMyPrescriptions(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        Long userId = Integer.toUnsignedLong(email.hashCode());
        return ResponseEntity.ok(
            prescriptionService.getMyPrescriptions(userId));
    }

    @Hidden
    @PutMapping("/{prescriptionId}/assign-order/{orderId}")
    public ResponseEntity<PrescriptionResponseDTO> assignOrder(
            @PathVariable Long prescriptionId,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(
            prescriptionService.assignOrder(prescriptionId, orderId));
    }

    private static class PrescriptionUploadRequest {
        @Schema(type = "string", format = "binary", description = "Prescription image or PDF file")
        public MultipartFile file;
    }
}
