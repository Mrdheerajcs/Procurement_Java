package com.procurement.controller;

import com.procurement.dto.request.ContractRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.ContractDTO;
import com.procurement.dto.responce.WorkOrderDTO;
import com.procurement.entity.Contract;
import com.procurement.entity.Vendor;
import com.procurement.helper.CurrentUser;
import com.procurement.repository.ContractRepository;
import com.procurement.repository.VendorRepository;
import com.procurement.service.ContractService;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractRepository contractRepository;
    private final VendorRepository vendorRepository;
    private final ContractService contractService;

    @PostMapping
    public ResponseEntity<ApiResponse<ContractDTO>> createContract(@RequestBody ContractRequest request) {
        log.info("Creating contract: {}", request.getContractNo());
        return contractService.createContract(request);
    }

    @GetMapping("/{contractId}")
    public ResponseEntity<ApiResponse<ContractDTO>> getContractById(@PathVariable Long contractId) {
        log.info("Fetching contract: {}", contractId);
        return contractService.getContractById(contractId);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContractDTO>>> getAllContracts() {
        log.info("Fetching all contracts");
        return contractService.getAllContracts();
    }

    @GetMapping("/my-contracts")
    public ResponseEntity<ApiResponse<List<ContractDTO>>> getMyContracts() {
        log.info("Fetching my contracts");
        return contractService.getMyContracts();
    }

    @PatchMapping("/{contractId}/status")
    public ResponseEntity<ApiResponse<ContractDTO>> updateContractStatus(
            @PathVariable Long contractId,
            @RequestParam String status) {
        log.info("Updating contract {} status to: {}", contractId, status);
        return contractService.updateContractStatus(contractId, status);
    }

    @PostMapping("/{contractId}/upload-pbg")
    public ResponseEntity<ApiResponse<String>> uploadPBG(
            @PathVariable Long contractId,
            @RequestParam("file") MultipartFile file) {
        log.info("Uploading PBG for contract: {}", contractId);
        return contractService.uploadPBG(contractId, file);
    }

    @GetMapping("/{contractId}/download-work-order")
    public ResponseEntity<byte[]> downloadWorkOrder(@PathVariable Long contractId) {
        ResponseEntity<ApiResponse<byte[]>> response = contractService.downloadWorkOrder(contractId);
        ApiResponse<byte[]> apiResponse = response.getBody();

        if (apiResponse != null && apiResponse.getData() != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=work_order_" + contractId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(apiResponse.getData());
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{contractId}/view-work-order")
    public ResponseEntity<ApiResponse<WorkOrderDTO>> viewWorkOrder(@PathVariable Long contractId) {
        log.info("Viewing work order for contract: {}", contractId);
        return contractService.viewWorkOrder(contractId);
    }

    @GetMapping("/by-status")
    public ResponseEntity<ApiResponse<List<ContractDTO>>> getContractsByStatus(@RequestParam(required = false) String status) {
        log.info("Fetching contracts with status: {}", status);
        return contractService.getContractsByStatus(status);
    }

    @GetMapping("/{contractId}/generate-work-order")
    public ResponseEntity<byte[]> generateWorkOrderPDF(@PathVariable Long contractId) {
        log.info("Generating Work Order PDF for contract: {}", contractId);
        ResponseEntity<ApiResponse<byte[]>> response = contractService.generateWorkOrderPDF(contractId);
        ApiResponse<byte[]> apiResponse = response.getBody();
        if (apiResponse != null && apiResponse.getData() != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=WorkOrder_" + contractId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(apiResponse.getData());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/view-pbg")
    public ResponseEntity<byte[]> viewPbg(@RequestParam String filePath) {
        log.info("Viewing PBG file: {}", filePath);
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.error("PBG file not found: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = Files.readAllBytes(path);
            String fileName = path.getFileName().toString();

            // Determine content type
            String contentType = "application/pdf";
            if (fileName.toLowerCase().endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (fileName.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(fileContent);
        } catch (IOException e) {
            log.error("Error viewing PBG file: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}