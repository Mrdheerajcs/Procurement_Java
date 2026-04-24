package com.procurement.controller;

import com.procurement.dto.request.*;
import com.procurement.dto.responce.*;
import com.procurement.service.MprRegServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/mpr")
@RequiredArgsConstructor
public class MprController {
    private final MprRegServices mprRegServices;

    @Value("${app.upload.base-dir:C:/uploads}")
    private String baseDir;

    // CREATE MPR with documents
    @PostMapping("/registration/with-files")
    public ResponseEntity<ApiResponse<MprDto>> mprRegWithFiles(
            @RequestPart("mprData") MprRequest request,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents) {

        log.info("Registering MPR with {} documents", documents != null ? documents.size() : 0);

        if (documents != null && !documents.isEmpty()) {
            String docPath = saveMprDocuments(documents, request.getMprNo());
            request.setDocumentPath(docPath);
        }

        return mprRegServices.mprReg(request);
    }

    // UPDATE MPR with documents
    @PutMapping("/update-with-files")
    public ResponseEntity<ApiResponse<String>> updateMprWithFiles(
            @RequestPart("mprData") MprUpdateRequest request,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents) {

        if (documents != null && !documents.isEmpty()) {
            String docPath = saveMprDocuments(documents, request.getMprNo());
            mprRegServices.updateDocumentPath(request.getMprId(), docPath);
        }

        return mprRegServices.updateMpr(request);
    }

    // GET MPR documents for viewing
    @GetMapping("/documents/{mprId}")
    public ResponseEntity<ApiResponse<List<MprDocumentDto>>> getMprDocuments(@PathVariable Long mprId) {
        log.info("Fetching documents for MPR: {}", mprId);
        return mprRegServices.getMprDocuments(mprId);
    }

    // DOWNLOAD single document
    @GetMapping("/documents/download")
    public ResponseEntity<byte[]> downloadDocument(@RequestParam String filePath) {
        try {
            Path path = Paths.get(filePath);
            byte[] fileContent = Files.readAllBytes(path);
            String fileName = path.getFileName().toString();

            return ResponseEntity.ok()
                    .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                    .body(fileContent);
        } catch (IOException e) {
            log.error("Error downloading file: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    private String saveMprDocuments(List<MultipartFile> files, String mprNo) {
        try {
            // Use UploadConfig directory pattern
            String uploadDir = baseDir + "/mpr/" + mprNo;
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            List<String> savedPaths = new ArrayList<>();
            for (MultipartFile file : files) {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename().replaceAll("\\s+", "_");
                File dest = new File(dir, fileName);
                file.transferTo(dest);
                savedPaths.add(dest.getAbsolutePath());
            }
            return String.join(",", savedPaths);
        } catch (IOException e) {
            log.error("Error saving MPR documents", e);
            return null;
        }
    }

    // Existing methods...
    @PostMapping("/registration")
    public ResponseEntity<ApiResponse<MprDto>> mprReg(@RequestBody MprRequest request) {
        log.info("Registering Mpr...");
        return mprRegServices.mprReg(request);
    }

    @PutMapping("/approve")
    public ResponseEntity<ApiResponse<MprDetailDTO>> mprApproval(@RequestBody MprApprovalRequest request) {
        log.info("Mpr approval...");
        return mprRegServices.mprApproval(request);
    }

    @GetMapping("/getallbyStatus")
    public ResponseEntity<ApiResponse<List<MprResponse>>> getAllMprData(@RequestParam String status) {
        log.info("Fetching all Mpr with status: {}", status);
        return mprRegServices.getAllMprs(status);
    }

    @GetMapping("/getallbyMultiStatus")
    public ResponseEntity<ApiResponse<List<MprResponse>>> getAllMprDataByMultiStatus(@RequestParam List<String> status) {
        log.info("Fetching all Mpr with statuses: {}", status);
        return mprRegServices.getAllMprDataByMultiStatus(status);
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<String>> updateMpr(@RequestBody MprUpdateRequest request) {
        return mprRegServices.updateMpr(request);
    }

    @PostMapping("/publish")
    public ResponseEntity<ApiResponse<String>> publishTender(
            @RequestPart("data") TenderRequest request,
            @RequestPart(value = "nitDoc", required = false) MultipartFile nitDoc,
            @RequestPart(value = "boqDoc", required = false) MultipartFile boqDoc,
            @RequestPart(value = "techDoc", required = false) MultipartFile techDoc,
            @RequestPart(value = "otherDocs", required = false) List<MultipartFile> otherDocs) throws IOException {

        return mprRegServices.publishTender(request, nitDoc, boqDoc, techDoc, otherDocs);
    }
}