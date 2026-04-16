package com.procurement.controller;

import com.procurement.dto.responce.ApiResponse;
import com.procurement.entity.Contract;
import com.procurement.entity.TenderHeader;
import com.procurement.entity.Vendor;
import com.procurement.helper.CurrentUser;
import com.procurement.repository.ContractRepository;
import com.procurement.repository.TenderHeaderRepository;
import com.procurement.repository.VendorRepository;
import com.procurement.service.AuditService;
import com.procurement.service.EmailService;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tenders")
@RequiredArgsConstructor
public class TenderController {

    private final TenderHeaderRepository tenderRepository;
    private final AuditService auditService;

    private final EmailService emailService;

    private final ContractRepository contractRepository;
    private final VendorRepository vendorRepository;


    @GetMapping("/published")
    public ResponseEntity<ApiResponse<List<TenderHeader>>> getPublishedTenders() {
        log.info("Fetching all published tenders");
        // ✅ Check BOTH status fields
        List<TenderHeader> tenders = tenderRepository.findByTenderStatus("PUBLISHED");
        if (tenders == null || tenders.isEmpty()) {
            // Fallback: check status column
            tenders = tenderRepository.findByStatus("PUBLISHED");
        }
        return ResponseUtil.success(tenders, "Published tenders retrieved");
    }

    // ✅ GET tender by ID
    @GetMapping("/{tenderId}")
    public ResponseEntity<ApiResponse<TenderHeader>> getTenderById(@PathVariable Long tenderId) {
        log.info("Fetching tender by ID: {}", tenderId);
        TenderHeader tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new RuntimeException("Tender not found"));
        return ResponseUtil.success(tender, "Tender details retrieved");
    }

    // ✅ GET all tenders (for admin evaluation)
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<TenderHeader>>> getAllTenders(
            @RequestParam(required = false) String status) {
        log.info("Fetching all tenders with status: {}", status);
        List<TenderHeader> tenders;
        if (status != null && !status.isEmpty()) {
            tenders = tenderRepository.findByTenderStatus(status);
        } else {
            tenders = tenderRepository.findAll();
        }
        return ResponseUtil.success(tenders, "Tenders retrieved");
    }

    @PostMapping("/{tenderId}/award")
    public ResponseEntity<ApiResponse<String>> awardContract(
            @PathVariable Long tenderId,
            @RequestParam Long vendorId) {
        log.info("Awarding tender {} to vendor {}", tenderId, vendorId);

        TenderHeader tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new RuntimeException("Tender not found"));

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        tender.setTenderStatus("AWARDED");
        tenderRepository.save(tender);

        // ✅ Create contract record
        Contract contract = Contract.builder()
                .contractNo("CONT/" + System.currentTimeMillis())
                .tenderId(tender.getTenderId())
                .tenderNo(tender.getTenderNo())
                .tenderTitle(tender.getTenderTitle())
                .vendorId(vendor.getVendorId())
                .vendorName(vendor.getVendorName())
                .awardDate(LocalDate.now())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(12))
                .amount(tender.getEmdAmount() != null ? tender.getEmdAmount() : BigDecimal.ZERO)
                .status("AWARDED")
                .createdBy(CurrentUser.getCurrentUserOrThrow().getUsername())
                .createdAt(LocalDateTime.now())
                .build();

        contractRepository.save(contract);

        auditService.log("AWARD_CONTRACT", "TenderHeader", tenderId,
                null, "Awarded to vendor: " + vendorId);

        // Send email to vendor
        // emailService.sendContractAwardEmail(vendor.getEmailId(), tender.getTenderNo(), vendor.getVendorName(), contract.getAmount());

        return ResponseUtil.success("Contract awarded successfully to vendor", "Success");
    }


    // ✅ Get tenders by status (for approval list)
    @GetMapping("/by-status/{status}")
    public ResponseEntity<ApiResponse<List<TenderHeader>>> getTendersByStatus(@PathVariable String status) {
        log.info("Fetching tenders with status: {}", status);
        List<TenderHeader> tenders = tenderRepository.findByTenderStatus(status);
        return ResponseUtil.success(tenders, "Tenders retrieved");
    }



    // ✅ Reject tender
    @PutMapping("/{tenderId}/reject")
    public ResponseEntity<ApiResponse<TenderHeader>> rejectTender(
            @PathVariable Long tenderId,
            @RequestParam String reason) {
        log.info("Rejecting tender: {} with reason: {}", tenderId, reason);

        TenderHeader tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new RuntimeException("Tender not found"));

        String oldStatus = tender.getTenderStatus();

        tender.setTenderStatus("REJECTED");
        tender.setRejectionReason(reason);
        tender.setRejectedAt(LocalDateTime.now());

        TenderHeader saved = tenderRepository.save(tender);

        emailService.sendTenderRejectionEmail(
                tender.getCreatedBy() + "@example.com",
                tender.getTenderNo(),
                tender.getTenderTitle(),
                reason
        );

        // Audit log
        auditService.log("REJECT_TENDER", "TenderHeader", tenderId, oldStatus, "REJECTED", reason);

        return ResponseUtil.success(saved, "Tender rejected");
    }

    // ✅ Submit tender for approval (from DRAFT to PENDING_APPROVAL)
    @PutMapping("/{tenderId}/submit-for-approval")
    public ResponseEntity<ApiResponse<TenderHeader>> submitForApproval(@PathVariable Long tenderId) {
        log.info("Submitting tender for approval: {}", tenderId);

        TenderHeader tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new RuntimeException("Tender not found"));

        if (!"DRAFT".equals(tender.getTenderStatus())) {
            return ResponseUtil.error("Only DRAFT tenders can be submitted for approval");
        }

        tender.setTenderStatus("PENDING_APPROVAL");
        TenderHeader saved = tenderRepository.save(tender);

        auditService.log("SUBMIT_FOR_APPROVAL", "TenderHeader", tenderId, "DRAFT", "PENDING_APPROVAL", null);

        return ResponseUtil.success(saved, "Tender submitted for approval");
    }

    // ✅ Approve tender - FIXED
    @PutMapping("/{tenderId}/approve")
    public ResponseEntity<ApiResponse<TenderHeader>> approveTender(
            @PathVariable Long tenderId,
            @RequestParam(required = false) String remarks) {
        log.info("Approving tender: {}", tenderId);

        TenderHeader tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new RuntimeException("Tender not found"));

        String oldStatus = tender.getTenderStatus();

        // ✅ FIX: Set BOTH status fields
        tender.setTenderStatus("APPROVED");
        tender.setStatus("APPROVED");  // IMPORTANT: Set both
        tender.setApprovedBy(CurrentUser.getCurrentUserOrThrow().getUsername());
        tender.setApprovedAt(LocalDateTime.now());

        TenderHeader saved = tenderRepository.save(tender);

        auditService.log("APPROVE_TENDER", "TenderHeader", tenderId, oldStatus, "APPROVED", remarks);

        return ResponseUtil.success(saved, "Tender approved successfully");
    }

    // ✅ Publish tender - FIXED
    @PutMapping("/{tenderId}/publish")
    public ResponseEntity<ApiResponse<TenderHeader>> publishTender(@PathVariable Long tenderId) {
        log.info("Publishing tender: {}", tenderId);

        TenderHeader tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new RuntimeException("Tender not found"));

        if (!"APPROVED".equals(tender.getTenderStatus())) {
            return ResponseUtil.error("Only APPROVED tenders can be published");
        }

        // ✅ FIX: Set BOTH status fields
        tender.setTenderStatus("PUBLISHED");
        tender.setStatus("PUBLISHED");  // IMPORTANT: Set both
        tender.setPublishedBy(CurrentUser.getCurrentUserOrThrow().getUsername());
        tender.setPublishedAt(LocalDateTime.now());

        TenderHeader saved = tenderRepository.save(tender);

        auditService.log("PUBLISH_TENDER", "TenderHeader", tenderId, "APPROVED", "PUBLISHED", null);

        return ResponseUtil.success(saved, "Tender published successfully");
    }
}