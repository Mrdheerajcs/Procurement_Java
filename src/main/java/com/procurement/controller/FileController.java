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
import java.net.URLEncoder;
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
    private Path resolvePath(String relativePath) {
        try {
            Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
            Path targetPath = basePath.resolve(relativePath).normalize();

            if (!targetPath.startsWith(basePath)) {
                throw new RuntimeException("Invalid file path");
            }

            if (!Files.exists(targetPath)) {
                throw new RuntimeException("File not found");
            }

            return targetPath;

        } catch (Exception e) {
            throw new RuntimeException("Invalid file path");
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

        Path file = resolvePath(path);
        Resource resource = new UrlResource(file.toUri());

        String contentType = Files.probeContentType(file);
        if (contentType == null) contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
    // ================= DOWNLOAD FILE =================
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String path) throws IOException {

        Path file = resolvePath(path);
        Resource resource = new UrlResource(file.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFileName().toString() + "\"")
                .body(resource);
    }
}