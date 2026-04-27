package com.procurement.service.impl;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.procurement.dto.request.ContractRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.ContractDTO;
import com.procurement.dto.responce.WorkOrderDTO;
import com.procurement.entity.Contract;
import com.procurement.helper.CurrentUser;
import com.procurement.helper.GeneratePBGPDF;
import com.procurement.repository.ContractRepository;
import com.procurement.service.ContractService;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.Document;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;

    @Value("${app.upload.base-dir:C:/uploads}")
    private String baseDir;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<ContractDTO>> createContract(ContractRequest request) {
        log.info("Creating contract: {}", request.getContractNo());

        // Check duplicate
        if (contractRepository.existsByContractNo(request.getContractNo())) {
            return ResponseUtil.error("Contract number already exists");
        }

        Contract contract = Contract.builder()
                .contractNo(request.getContractNo())
                .tenderId(request.getTenderId())
                .tenderNo(request.getTenderNo())
                .tenderTitle(request.getTenderTitle())
                .vendorId(request.getVendorId())
                .vendorName(request.getVendorName())
                .awardDate(request.getAwardDate())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .amount(request.getAmount())
                .status(request.getStatus() != null ? request.getStatus() : "AWARDED")
                .createdBy(CurrentUser.getCurrentUserOrThrow().getUsername())
                .createdAt(LocalDateTime.now())
                .build();

        Contract saved = contractRepository.save(contract);
        return ResponseUtil.success(toDTO(saved), "Contract created successfully");
    }

    @Override
    public ResponseEntity<ApiResponse<ContractDTO>> getContractById(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        return ResponseUtil.success(toDTO(contract));
    }

    @Override
    public ResponseEntity<ApiResponse<List<ContractDTO>>> getAllContracts() {
        List<ContractDTO> contracts = contractRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseUtil.success(contracts);
    }

    @Override
    public ResponseEntity<ApiResponse<List<ContractDTO>>> getMyContracts() {
        String currentUser = CurrentUser.getCurrentUserOrThrow().getUsername();

        // For vendor - get by vendorId (you need to map username to vendorId)
        // For now, fetch all - you can enhance later
        List<ContractDTO> contracts = contractRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseUtil.success(contracts);
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<ContractDTO>> updateContractStatus(Long contractId, String status) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        contract.setStatus(status);
        contract.setUpdatedAt(LocalDateTime.now());

        Contract saved = contractRepository.save(contract);
        return ResponseUtil.success(toDTO(saved), "Status updated to " + status);
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<String>> uploadPBG(Long contractId, MultipartFile file) {
        log.info("Uploading PBG for contract: {}", contractId);

        // 1. Validate file
        if (file == null || file.isEmpty()) {
            return ResponseUtil.error("No file provided");
        }

        // 2. Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            return ResponseUtil.error("Only PDF files are allowed for PBG");
        }

        // 3. Validate file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseUtil.error("PBG file size must be less than 5MB");
        }

        // 4. Find contract
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        try {
            // 5. Create directory if not exists (fallback)
            String uploadDir = baseDir + "/contracts/pbg/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    log.error("Failed to create directory: {}", uploadDir);
                    return ResponseUtil.error("Failed to create upload directory");
                }
                log.info("Created directory: {}", uploadDir);
            }

            // 6. Generate unique filename
            String timestamp = String.valueOf(System.currentTimeMillis());
            String fileName = "PBG_" + contract.getContractNo() + "_" + timestamp + ".pdf";
            // Remove any invalid characters from contract number
            fileName = fileName.replaceAll("[^a-zA-Z0-9_\\-\\.]", "_");

            File dest = new File(dir, fileName);

            // 7. Save file
            file.transferTo(dest);

            // 8. Verify file was saved
            if (!dest.exists() || dest.length() == 0) {
                log.error("File was not saved properly: {}", dest.getAbsolutePath());
                return ResponseUtil.error("Failed to save PBG file");
            }

            // 9. Update contract with file path
            contract.setPbgPath(dest.getAbsolutePath());
            contract.setUpdatedAt(LocalDateTime.now());
            contractRepository.save(contract);

            log.info("PBG uploaded successfully: {}", dest.getAbsolutePath());
            return ResponseUtil.success("PBG uploaded successfully");

        } catch (IOException e) {
            log.error("Error uploading PBG: {}", e.getMessage());
            return ResponseUtil.error("Failed to upload PBG: " + e.getMessage());
        }
    }
    @Override
    public ResponseEntity<ApiResponse<byte[]>> downloadWorkOrder(Long contractId) {
        log.info("Downloading work order for contract: {}", contractId);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        byte[] pdfContent = GeneratePBGPDF.generateWorkOrderPDF(contract);

        return ResponseUtil.success(pdfContent, "Work order downloaded successfully");
    }
    @Override
    public ResponseEntity<ApiResponse<WorkOrderDTO>> viewWorkOrder(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        WorkOrderDTO workOrder = WorkOrderDTO.builder()
                .workOrderNo("WO/" + contract.getContractNo())
                .contractNo(contract.getContractNo())
                .tenderTitle(contract.getTenderTitle())
                .vendorName(contract.getVendorName())
                .issueDate(contract.getAwardDate())
                .deliveryDate(contract.getEndDate())
                .totalAmount(contract.getAmount())
                .status(contract.getStatus())
                .pdfPath(null) // Will be generated on download
                .build();

        return ResponseUtil.success(workOrder);
    }



    private ContractDTO toDTO(Contract contract) {
        return ContractDTO.builder()
                .contractId(contract.getContractId())
                .contractNo(contract.getContractNo())
                .tenderId(contract.getTenderId())
                .tenderNo(contract.getTenderNo())
                .tenderTitle(contract.getTenderTitle())
                .vendorId(contract.getVendorId())
                .vendorName(contract.getVendorName())
                .awardDate(contract.getAwardDate())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .amount(contract.getAmount())
                .status(contract.getStatus())
                .pbgPath(contract.getPbgPath())
                .createdBy(contract.getCreatedBy())
                .createdAt(contract.getCreatedAt())
                .build();
    }

    @Override
    public ResponseEntity<ApiResponse<List<ContractDTO>>> getContractsByStatus(String status) {
        log.info("Fetching contracts with status: {}", status);
        List<Contract> contracts;
        if (status == null || status.equalsIgnoreCase("ALL")) {
            contracts = contractRepository.findAll();
        } else {
            contracts = contractRepository.findByStatus(status);
        }
        List<ContractDTO> dtos = contracts.stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseUtil.success(dtos);
    }

    @Override
    public ResponseEntity<ApiResponse<byte[]>> generateWorkOrderPDF(Long contractId) {
        log.info("Generating Work Order PDF for contract: {}", contractId);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        // ✅ Use the helper class
        byte[] pdfContent = GeneratePBGPDF.generateWorkOrderPDF(contract);

        return ResponseUtil.success(pdfContent, "Work Order PDF generated successfully");
    }
}