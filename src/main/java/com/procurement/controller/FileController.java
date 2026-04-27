package com.procurement.controller;

import com.procurement.dto.responce.DocumentDTO;
import com.procurement.entity.*;
import com.procurement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final TenderDocumentRepository tenderRepo;
    private final BidTechnicalRepository techRepo;
    private final BidFinancialRepository finRepo;

    @Value("${app.upload.base-dir:C:/uploads}")
    private String baseDir;

    // ================= SAFE PATH =================
    private Path resolvePath(String filePath) {
        try {
            log.info("Resolving path: {}", filePath);

            String decodedPath = URLDecoder.decode(filePath, StandardCharsets.UTF_8.name());
            log.info("Decoded path: {}", decodedPath);

            Path targetPath = Paths.get(decodedPath).toAbsolutePath().normalize();
            log.info("Absolute path: {}", targetPath);

            if (!Files.exists(targetPath)) {
                log.error("File not found: {}", targetPath);
                throw new RuntimeException("File not found: " + targetPath);
            }

            log.info("File exists, size: {}", Files.size(targetPath));
            return targetPath;

        } catch (Exception e) {
            log.error("Path resolution error: {}", e.getMessage());
            throw new RuntimeException("Invalid file path: " + e.getMessage());
        }
    }

    // ================= DOCUMENT LIST =================
    @GetMapping("/documents/{module}/{id}")
    public List<DocumentDTO> getDocuments(@PathVariable String module, @PathVariable Long id) {

        switch (module) {

            case "TENDER":
                return tenderRepo.findByTenderId(id).stream()
                        .map(d -> DocumentDTO.builder()
                                .fileName(d.getFileName())
                                .filePath(d.getFilePath())
                                .category(d.getDocCategory())
                                .build())
                        .toList();

            case "TECH":
                BidTechnical tech = techRepo.findById(id).orElseThrow();
                List<DocumentDTO> techDocs = new ArrayList<>();

                if (tech.getPanCardPath() != null)
                    techDocs.add(DocumentDTO.builder().fileName("PAN Card").filePath(tech.getPanCardPath()).category("PAN").build());

                if (tech.getGstCertPath() != null)
                    techDocs.add(DocumentDTO.builder().fileName("GST Certificate").filePath(tech.getGstCertPath()).category("GST").build());

                if (tech.getExperienceCertPath() != null)
                    techDocs.add(DocumentDTO.builder().fileName("Experience Certificate").filePath(tech.getExperienceCertPath()).category("EXP").build());

                return techDocs;

            case "FIN":
                BidFinancial fin = finRepo.findById(id).orElseThrow();
                List<DocumentDTO> finDocs = new ArrayList<>();

                if (fin.getBoqFilePath() != null)
                    finDocs.add(DocumentDTO.builder().fileName("BOQ").filePath(fin.getBoqFilePath()).category("BOQ").build());

                if (fin.getEmdReceiptPath() != null)
                    finDocs.add(DocumentDTO.builder().fileName("EMD Receipt").filePath(fin.getEmdReceiptPath()).category("EMD").build());

                return finDocs;

            default:
                throw new RuntimeException("Invalid module");
        }
    }

    // ================= VIEW FILE =================
    @GetMapping("/view")
    public ResponseEntity<Resource> viewFile(@RequestParam String path) throws IOException {
        log.info("=== VIEW FILE REQUEST ===");
        log.info("Requested path param: {}", path);

        try {
            Path file = resolvePath(path);
            Resource resource = new UrlResource(file.toUri());

            String contentType = Files.probeContentType(file);
            if (contentType == null) contentType = "application/octet-stream";

            log.info("Serving file: {}, content-type: {}", file.getFileName(), contentType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName().toString() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("View error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ================= DOWNLOAD FILE =================
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String path) throws IOException {
        log.info("Downloading file: {}", path);

        Path file = resolvePath(path);
        Resource resource = new UrlResource(file.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFileName().toString() + "\"")
                .body(resource);
    }
}