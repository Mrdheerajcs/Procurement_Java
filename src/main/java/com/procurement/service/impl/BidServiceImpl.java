package com.procurement.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.procurement.dto.request.BidFinalSubmissionRequest;
import com.procurement.dto.request.BidFinancialRequest;
import com.procurement.dto.request.BidTechnicalRequest;
import com.procurement.dto.request.ClarificationRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.BidFinancialResponse;
import com.procurement.dto.responce.BidTechnicalResponse;
import com.procurement.dto.responce.ClarificationResponse;
import com.procurement.entity.*;
import com.procurement.helper.CurrentUser;
import com.procurement.repository.*;
import com.procurement.service.AuditService;
import com.procurement.service.BidService;
import com.procurement.util.EncryptionUtil;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final TenderHeaderRepository tenderRepository;
    private final VendorRepository vendorRepository;
    private final BidTechnicalRepository bidTechnicalRepository;
    private final BidFinancialRepository bidFinancialRepository;
    private final EncryptionUtil encryptionUtil;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Value("${app.upload.bids-dir:C:/uploads/bids}")
    private String UPLOAD_DIR;

    // ==================== TECHNICAL BID ====================

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> submitTechnicalBid(BidTechnicalRequest request, MultipartFile[] files) {
        log.info("Submitting technical bid for tender: {}", request.getTenderId());

        Vendor vendor = getCurrentVendor();
        TenderHeader tender = getTenderAndValidate(request.getTenderId());

        if (bidTechnicalRepository.existsByTenderAndVendor(tender, vendor)) {
            return ResponseUtil.error("Technical bid already submitted for this tender");
        }

        if (!isTenderAcceptingBids(tender)) {
            return ResponseUtil.error("Tender is not accepting bids at this time");
        }

        BidTechnical bidTechnical = BidTechnical.builder()
                .tender(tender)
                .vendor(vendor)
                .companyName(request.getCompanyName())
                .gstNumber(request.getGstNumber())
                .panNumber(request.getPanNumber())
                .makeIndiaClass(request.getMakeIndiaClass())
                .bidderTurnover(request.getBidderTurnover())
                .oemTurnover(request.getOemTurnover())
                .oemName(request.getOemName())
                .authorizationDetails(request.getAuthorizationDetails())
                .msmeNumber(request.getMsmeNumber())
                .isMsme(request.getIsMsme() != null && request.getIsMsme())
                .submissionStatus("SUBMITTED")
                .evaluationStatus("PENDING")
                .submittedAt(LocalDateTime.now())
                .build();

        BidTechnical saved = bidTechnicalRepository.save(bidTechnical);
        log.info("Technical bid submitted with ID: {}", saved.getBidTechnicalId());

        auditService.log("SUBMIT_TECHNICAL_BID", "BidTechnical", saved.getBidTechnicalId(),
                null, objectMapper.valueToTree(saved).toString());

        return ResponseUtil.success(mapToTechnicalResponse(saved), "Technical bid submitted successfully");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> saveTechnicalDraft(BidTechnicalRequest request, MultipartFile[] files) {
        log.info("Saving technical bid as DRAFT for tender: {}", request.getTenderId());
        log.info("Files received: {}", files != null ? files.length : 0);

        Vendor vendor = getCurrentVendor();
        TenderHeader tender = getTenderAndValidate(request.getTenderId());

        Optional<BidTechnical> existing = bidTechnicalRepository.findByTenderAndVendor(tender, vendor);

        BidTechnical bidTechnical;

        if (existing.isPresent() && "SUBMITTED".equals(existing.get().getSubmissionStatus())) {
            return ResponseUtil.error("You have already submitted final bid. Cannot edit.");
        }

        if (existing.isPresent()) {
            bidTechnical = existing.get();
            updateTechnicalFields(bidTechnical, request);
            log.info("Updating existing technical draft with ID: {}", bidTechnical.getBidTechnicalId());
        } else {
            bidTechnical = BidTechnical.builder()
                    .tender(tender)
                    .vendor(vendor)
                    .submissionStatus("DRAFT")
                    .evaluationStatus("PENDING")
                    .build();
            updateTechnicalFields(bidTechnical, request);
            log.info("Creating new technical draft");
        }

        // FIRST save to generate ID
        BidTechnical saved = bidTechnicalRepository.save(bidTechnical);
        log.info("Saved technical draft with ID: {}", saved.getBidTechnicalId());

        // THEN save documents using the generated ID
        if (files != null && files.length > 0) {
            log.info("Processing {} files for bid ID: {}", files.length, saved.getBidTechnicalId());

            for (MultipartFile file : files) {
                String fieldName = file.getName();
                String originalFileName = file.getOriginalFilename();
                log.info("Processing file - Field: {}, Name: {}", fieldName, originalFileName);

                String savedPath = saveTechnicalDocument(file, saved.getBidTechnicalId());
                if (savedPath == null) {
                    log.error("Failed to save file: {}", originalFileName);
                    continue;
                }

                log.info("File saved at: {}", savedPath);

                switch (fieldName) {
                    case "experienceCertificate":
                        saved.setExperienceCertPath(savedPath);
                        break;
                    case "oemAuthorization":
                        saved.setOemAuthPath(savedPath);
                        break;
                    case "gstCertificate":
                        saved.setGstCertPath(savedPath);
                        break;
                    case "panCard":
                        saved.setPanCardPath(savedPath);
                        break;
                    case "msmeCertificate":
                        saved.setMsmeCertPath(savedPath);
                        break;
                    case "otherDocs":
                        String existingPath = saved.getOtherDocsPath();
                        saved.setOtherDocsPath(existingPath == null ? savedPath : existingPath + "," + savedPath);
                        break;
                    default:
                        log.warn("Unknown field: {}, skipping", fieldName);
                        break;
                }
            }

            saved = bidTechnicalRepository.save(saved);
            log.info("Updated technical draft with document paths");
        }

        log.info("Final paths - ExpCert: {}, OemAuth: {}, GstCert: {}, PanCard: {}, MsmeCert: {}, OtherDocs: {}",
                saved.getExperienceCertPath(), saved.getOemAuthPath(), saved.getGstCertPath(),
                saved.getPanCardPath(), saved.getMsmeCertPath(), saved.getOtherDocsPath());

        return ResponseUtil.success(mapToTechnicalResponse(saved), "Technical bid saved as draft");
    }

    // ==================== FINANCIAL BID ====================

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<BidFinancialResponse>> submitFinancialBid(BidFinancialRequest request, MultipartFile[] files) {
        log.info("Submitting financial bid for tender: {}", request.getTenderId());

        Vendor vendor = getCurrentVendor();
        TenderHeader tender = getTenderAndValidate(request.getTenderId());

        BidTechnical bidTechnical = bidTechnicalRepository.findById(request.getBidTechnicalId())
                .orElseThrow(() -> new RuntimeException("Technical bid not found"));

        if (!bidTechnical.getVendor().getVendorId().equals(vendor.getVendorId())) {
            return ResponseUtil.error("Unauthorized: This technical bid does not belong to you");
        }

        if (!"QUALIFIED".equals(bidTechnical.getEvaluationStatus())) {
            return ResponseUtil.error("Cannot submit financial bid: Technical bid is not qualified");
        }

        if (bidFinancialRepository.findByBidTechnical(bidTechnical).isPresent()) {
            return ResponseUtil.error("Financial bid already submitted");
        }

        BidFinancial bidFinancial = BidFinancial.builder()
                .tender(tender)
                .vendor(vendor)
                .bidTechnical(bidTechnical)
                .encryptedTotalBidAmount(encryptionUtil.encrypt(request.getTotalBidAmount().toString()))
                .encryptedGstPercent(encryptionUtil.encrypt(request.getGstPercent().toString()))
                .encryptedTotalCost(encryptionUtil.encrypt(request.getTotalCost().toString()))
                .encryptedBankName(encryptionUtil.encrypt(request.getBankName()))
                .encryptedAccountNumber(encryptionUtil.encrypt(request.getAccountNumber()))
                .encryptedIfscCode(encryptionUtil.encrypt(request.getIfscCode()))
                .encryptedEmdNumber(request.getEmdNumber() != null ? encryptionUtil.encrypt(request.getEmdNumber()) : null)
                .encryptedEmdValue(request.getEmdValue() != null ? encryptionUtil.encrypt(request.getEmdValue().toString()) : null)
                .emdExemptionDetails(request.getEmdExemptionDetails())
                .isRevealed("NO")
                .submittedAt(LocalDateTime.now())
                .build();

        BidFinancial saved = bidFinancialRepository.save(bidFinancial);
        log.info("Financial bid submitted with ID: {}", saved.getBidFinancialId());

        auditService.log("SUBMIT_FINANCIAL_BID", "BidFinancial", saved.getBidFinancialId(),
                null, "Financial bid submitted (encrypted)");

        return ResponseUtil.success(mapToFinancialResponse(saved), "Financial bid submitted successfully");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<BidFinancialResponse>> saveFinancialDraft(BidFinancialRequest request, MultipartFile[] files) {
        log.info("Saving financial bid as DRAFT for tender: {}", request.getTenderId());
        log.info("Financial data - TotalBidAmount: {}, GST: {}, TotalCost: {}",
                request.getTotalBidAmount(), request.getGstPercent(), request.getTotalCost());

        Vendor vendor = getCurrentVendor();
        TenderHeader tender = getTenderAndValidate(request.getTenderId());

        Optional<BidTechnical> existingTech = bidTechnicalRepository.findByTenderAndVendor(tender, vendor);
        if (existingTech.isEmpty()) {
            return ResponseUtil.error("Please save technical draft first");
        }

        BidTechnical bidTechnical = existingTech.get();

        if (!"DRAFT".equals(bidTechnical.getSubmissionStatus())) {
            return ResponseUtil.error("Technical bid already submitted. Cannot save financial draft.");
        }

        // ✅ Check if financial draft already exists
        Optional<BidFinancial> existingFin = bidFinancialRepository.findByBidTechnical(bidTechnical);

        BidFinancial bidFinancial;
        boolean isNew = false;

        if (existingFin.isPresent()) {
            bidFinancial = existingFin.get();
            log.info("Updating existing financial draft with ID: {}", bidFinancial.getBidFinancialId());
        } else {
            isNew = true;
            bidFinancial = new BidFinancial();
            bidFinancial.setTender(tender);
            bidFinancial.setVendor(vendor);
            bidFinancial.setBidTechnical(bidTechnical);
            bidFinancial.setIsRevealed("NO");
            log.info("Creating new financial draft");
        }

        // ✅ ENCRYPT AND SET ALL FIELDS (CRITICAL)
        String totalBidAmountStr = request.getTotalBidAmount() != null ? request.getTotalBidAmount().toString() : "0";
        String gstPercentStr = request.getGstPercent() != null ? request.getGstPercent().toString() : "0";
        String totalCostStr = request.getTotalCost() != null ? request.getTotalCost().toString() : "0";

        log.info("Encrypting - TotalBidAmount: {}, GST: {}, TotalCost: {}", totalBidAmountStr, gstPercentStr, totalCostStr);

        bidFinancial.setEncryptedTotalBidAmount(encryptionUtil.encrypt(totalBidAmountStr));
        bidFinancial.setEncryptedGstPercent(encryptionUtil.encrypt(gstPercentStr));
        bidFinancial.setEncryptedTotalCost(encryptionUtil.encrypt(totalCostStr));

        if (request.getBankName() != null && !request.getBankName().isEmpty()) {
            bidFinancial.setEncryptedBankName(encryptionUtil.encrypt(request.getBankName()));
        }
        if (request.getAccountNumber() != null && !request.getAccountNumber().isEmpty()) {
            bidFinancial.setEncryptedAccountNumber(encryptionUtil.encrypt(request.getAccountNumber()));
        }
        if (request.getIfscCode() != null && !request.getIfscCode().isEmpty()) {
            bidFinancial.setEncryptedIfscCode(encryptionUtil.encrypt(request.getIfscCode()));
        }
        if (request.getEmdNumber() != null && !request.getEmdNumber().isEmpty()) {
            bidFinancial.setEncryptedEmdNumber(encryptionUtil.encrypt(request.getEmdNumber()));
        }
        if (request.getEmdValue() != null) {
            bidFinancial.setEncryptedEmdValue(encryptionUtil.encrypt(request.getEmdValue().toString()));
        }

        bidFinancial.setEmdExemptionDetails(request.getEmdExemptionDetails());

        // ✅ Set timestamps
        if (isNew) {
            bidFinancial.setSubmittedAt(LocalDateTime.now());
        }
        bidFinancial.setUpdatedAt(LocalDateTime.now());

        // ✅ FIRST save to generate ID and store encrypted data
        BidFinancial saved = bidFinancialRepository.save(bidFinancial);
        log.info("Saved financial draft with ID: {}, Encrypted data saved", saved.getBidFinancialId());

        // ✅ THEN save documents
        if (files != null && files.length > 0) {
            log.info("Processing {} financial files", files.length);

            for (MultipartFile file : files) {
                String fieldName = file.getName();
                String savedPath = saveFinancialDocumentFile(file, saved.getBidFinancialId());
                if (savedPath == null) continue;

                log.info("Saving financial file - Field: {}, Path: {}", fieldName, savedPath);

                switch (fieldName) {
                    case "boqFile":
                        saved.setBoqFilePath(savedPath);
                        break;
                    case "priceBreakup":
                        saved.setPriceBreakupPath(savedPath);
                        break;
                    case "emdReceipt":
                        saved.setEmdReceiptPath(savedPath);
                        break;
                    case "otherFinancialDocs":
                        String existing = saved.getOtherFinancialDocsPath();
                        saved.setOtherFinancialDocsPath(existing == null ? savedPath : existing + "," + savedPath);
                        break;
                }
            }

            saved = bidFinancialRepository.save(saved);
            log.info("Updated financial draft with document paths");
        }

        // ✅ Verify data was saved
        BidFinancial verify = bidFinancialRepository.findById(saved.getBidFinancialId()).orElse(null);
        if (verify != null) {
            log.info("Verification - EncryptedTotalBidAmount: {}", verify.getEncryptedTotalBidAmount());
        }

        return ResponseUtil.success(mapToFinancialResponse(saved), "Financial draft saved");
    }
    @Override
    @Transactional
    public ResponseEntity<ApiResponse<BidFinancialResponse>> submitFinalBid(BidFinalSubmissionRequest request, MultipartFile[] files) {
        log.info("Submitting final bid for tender: {}", request.getTenderId());

        Vendor vendor = getCurrentVendor();
        TenderHeader tender = getTenderAndValidate(request.getTenderId());

        // Get or create technical bid
        BidTechnical bidTechnical = bidTechnicalRepository.findByTenderAndVendor(tender, vendor)
                .orElse(new BidTechnical());

        // Update technical fields
        updateTechnicalFields(bidTechnical, request);
        bidTechnical.setTender(tender);
        bidTechnical.setVendor(vendor);
        bidTechnical.setSubmissionStatus("SUBMITTED");
        bidTechnical.setEvaluationStatus("PENDING");
        bidTechnical.setSubmittedAt(LocalDateTime.now());

        BidTechnical savedTechnical = bidTechnicalRepository.save(bidTechnical);
        log.info("Technical bid saved with ID: {}", savedTechnical.getBidTechnicalId());

        // ✅ Check if financial bid already exists (draft or submitted)
        Optional<BidFinancial> existingFinancial = bidFinancialRepository.findByBidTechnical(savedTechnical);

        BidFinancial bidFinancial;
        if (existingFinancial.isPresent()) {
            // Update existing financial bid
            bidFinancial = existingFinancial.get();
            log.info("Updating existing financial bid with ID: {}", bidFinancial.getBidFinancialId());

            bidFinancial.setEncryptedTotalBidAmount(encryptionUtil.encrypt(request.getTotalBidAmount().toString()));
            bidFinancial.setEncryptedGstPercent(encryptionUtil.encrypt(request.getGstPercent().toString()));
            bidFinancial.setEncryptedTotalCost(encryptionUtil.encrypt(request.getTotalCost().toString()));
            bidFinancial.setEncryptedBankName(encryptionUtil.encrypt(request.getBankName()));
            bidFinancial.setEncryptedAccountNumber(encryptionUtil.encrypt(request.getAccountNumber()));
            bidFinancial.setEncryptedIfscCode(encryptionUtil.encrypt(request.getIfscCode()));
            bidFinancial.setEncryptedEmdNumber(request.getEmdNumber() != null ? encryptionUtil.encrypt(request.getEmdNumber()) : null);
            bidFinancial.setEncryptedEmdValue(request.getEmdValue() != null ? encryptionUtil.encrypt(request.getEmdValue().toString()) : null);
            bidFinancial.setEmdExemptionDetails(request.getEmdExemptionDetails());
            bidFinancial.setUpdatedAt(LocalDateTime.now());
        } else {
            // Create new financial bid
            bidFinancial = BidFinancial.builder()
                    .tender(tender)
                    .vendor(vendor)
                    .bidTechnical(savedTechnical)
                    .encryptedTotalBidAmount(encryptionUtil.encrypt(request.getTotalBidAmount().toString()))
                    .encryptedGstPercent(encryptionUtil.encrypt(request.getGstPercent().toString()))
                    .encryptedTotalCost(encryptionUtil.encrypt(request.getTotalCost().toString()))
                    .encryptedBankName(encryptionUtil.encrypt(request.getBankName()))
                    .encryptedAccountNumber(encryptionUtil.encrypt(request.getAccountNumber()))
                    .encryptedIfscCode(encryptionUtil.encrypt(request.getIfscCode()))
                    .encryptedEmdNumber(request.getEmdNumber() != null ? encryptionUtil.encrypt(request.getEmdNumber()) : null)
                    .encryptedEmdValue(request.getEmdValue() != null ? encryptionUtil.encrypt(request.getEmdValue().toString()) : null)
                    .emdExemptionDetails(request.getEmdExemptionDetails())
                    .isRevealed("NO")
                    .submittedAt(LocalDateTime.now())
                    .build();
            log.info("Creating new financial bid");
        }

        BidFinancial saved = bidFinancialRepository.save(bidFinancial);
        log.info("Final bid submitted with ID: {}", saved.getBidFinancialId());

        auditService.log("SUBMIT_FINAL_BID", "BidFinancial", saved.getBidFinancialId(),
                null, "Final bid submitted");

        return ResponseUtil.success(mapToFinancialResponse(saved), "Bid submitted successfully!");
    }
    // ==================== EVALUATION ====================

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> evaluateTechnicalBid(
            Long bidTechnicalId, String status, Integer score, String remarks) {
        log.info("Evaluating technical bid: {} with status: {}", bidTechnicalId, status);

        BidTechnical bidTechnical = bidTechnicalRepository.findById(bidTechnicalId)
                .orElseThrow(() -> new RuntimeException("Technical bid not found"));

        String oldStatus = bidTechnical.getEvaluationStatus();

        bidTechnical.setEvaluationStatus(status);
        if (score != null) {
            bidTechnical.setEvaluationScore(score);
        }
        if (remarks != null && !remarks.isEmpty()) {
            bidTechnical.setEvaluationRemarks(remarks);
        }
        bidTechnical.setEvaluatedBy(CurrentUser.getCurrentUserOrThrow().getUsername());
        bidTechnical.setEvaluatedAt(LocalDateTime.now());

        BidTechnical saved = bidTechnicalRepository.save(bidTechnical);

        log.info("Technical bid evaluated: {} -> {}", oldStatus, status);

        auditService.log("EVALUATE_TECHNICAL_BID", "BidTechnical", bidTechnicalId,
                oldStatus, status, "Score: " + score + ", Remarks: " + remarks);

        return ResponseUtil.success(mapToTechnicalResponse(saved), "Technical bid evaluated successfully");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<List<BidFinancialResponse>>> revealFinancialBids(Long tenderId) {
        log.info("Revealing financial bids for tender: {}", tenderId);

        TenderHeader tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new RuntimeException("Tender not found"));

        // ✅ Get QUALIFIED active bids only
        List<BidTechnical> qualifiedBids = bidTechnicalRepository.findQualifiedActiveBids(tenderId);

        if (qualifiedBids.isEmpty()) {
            return ResponseUtil.error("No qualified active bids found to reveal");
        }

        List<BidFinancialResponse> responses = new ArrayList<>();

        for (BidTechnical bidTech : qualifiedBids) {
            Optional<BidFinancial> optFinancial = bidFinancialRepository.findByBidTechnical(bidTech);
            if (optFinancial.isPresent() && "NO".equals(optFinancial.get().getIsRevealed())) {
                bidFinancialRepository.revealFinancial(
                        bidTech.getBidTechnicalId(),
                        CurrentUser.getCurrentUserOrThrow().getUsername()
                );

                BidFinancial financial = optFinancial.get();
                financial.setIsRevealed("YES");
                responses.add(mapToFinancialResponse(financial));
            }
        }

        auditService.log("REVEAL_FINANCIAL_BIDS", "TenderHeader", tenderId,
                null, "Revealed " + responses.size() + " financial bids");

        return ResponseUtil.success(responses, "Financial bids revealed successfully");
    }



    // ==================== GETTERS ====================

    @Override
    public ResponseEntity<ApiResponse<List<BidTechnicalResponse>>> getTechnicalBidsByTender(Long tenderId, String status) {
        log.info("Fetching technical bids for tender: {} with status: {}", tenderId, status);

        TenderHeader tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new RuntimeException("Tender not found"));

        List<BidTechnical> bids = bidTechnicalRepository.findByTenderAndSubmissionStatus(tender, "SUBMITTED");

        if (status != null && !status.isEmpty()) {
            bids = bids.stream()
                    .filter(b -> status.equals(b.getEvaluationStatus()))
                    .collect(Collectors.toList());
        }

        List<BidTechnicalResponse> responses = bids.stream()
                .map(this::mapToTechnicalResponse)
                .collect(Collectors.toList());

        return ResponseUtil.success(responses, "Technical bids fetched successfully");
    }

    @Override
    public ResponseEntity<ApiResponse<List<BidFinancialResponse>>> getFinancialBidsByTender(Long tenderId) {
        log.info("Fetching financial bids for tender: {}", tenderId);

        TenderHeader tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new RuntimeException("Tender not found"));

        // ✅ Get active bids (exclude WITHDRAWN)
        List<BidTechnical> activeBids = bidTechnicalRepository.findActiveSubmittedBidsByTender(tender);

        if (activeBids.isEmpty()) {
            return ResponseUtil.success(new ArrayList<>(), "No active bids found for this tender");
        }

        // ✅ Check if all active bids are evaluated
        boolean allEvaluated = activeBids.stream()
                .allMatch(b -> "QUALIFIED".equals(b.getEvaluationStatus()) ||
                        "DISQUALIFIED".equals(b.getEvaluationStatus()));

        if (!allEvaluated) {
            long pendingCount = activeBids.stream()
                    .filter(b -> !"QUALIFIED".equals(b.getEvaluationStatus()) &&
                            !"DISQUALIFIED".equals(b.getEvaluationStatus()))
                    .count();
            log.info("Not all active vendors evaluated yet. Pending count: {}", pendingCount);
            return ResponseUtil.success(new ArrayList<>(),
                    "Technical evaluation not complete yet. " + pendingCount + " vendor(s) pending evaluation.");
        }

        // ✅ Get financial bids only for QUALIFIED vendors
        List<BidTechnical> qualifiedBids = activeBids.stream()
                .filter(b -> "QUALIFIED".equals(b.getEvaluationStatus()))
                .collect(Collectors.toList());

        if (qualifiedBids.isEmpty()) {
            return ResponseUtil.success(new ArrayList<>(), "No qualified vendors found for this tender");
        }

        List<BidFinancial> bids = new ArrayList<>();
        for (BidTechnical bidTech : qualifiedBids) {
            Optional<BidFinancial> bidFin = bidFinancialRepository.findByBidTechnical(bidTech);
            bidFin.ifPresent(bids::add);
        }

        List<BidFinancialResponse> responses = bids.stream()
                .map(this::mapToFinancialResponse)
                .collect(Collectors.toList());

        return ResponseUtil.success(responses, "Financial bids fetched successfully");
    }

    @Override
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> getTechnicalDraft(Long tenderId) {
        log.info("Getting technical draft for tender: {}", tenderId);

        Vendor vendor = getCurrentVendor();
        TenderHeader tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new RuntimeException("Tender not found"));

        Optional<BidTechnical> existing = bidTechnicalRepository.findByTenderAndVendor(tender, vendor);

        if (existing.isPresent() && "DRAFT".equals(existing.get().getSubmissionStatus())) {
            return ResponseUtil.success(mapToTechnicalResponse(existing.get()), "Draft retrieved");
        }

        return ResponseUtil.success(null, "No draft found");
    }

    @Override
    public ResponseEntity<ApiResponse<BidFinancialResponse>> getFinancialDraft(Long tenderId) {
        log.info("Getting financial draft for tender: {}", tenderId);

        Vendor vendor = getCurrentVendor();
        TenderHeader tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new RuntimeException("Tender not found"));

        Optional<BidTechnical> bidTechnical = bidTechnicalRepository.findByTenderAndVendor(tender, vendor);
        if (bidTechnical.isEmpty()) {
            return ResponseUtil.success(null, "No technical bid found");
        }

        Optional<BidFinancial> bidFinancial = bidFinancialRepository.findByBidTechnical(bidTechnical.get());
        if (bidFinancial.isEmpty()) {
            return ResponseUtil.success(null, "No financial draft found");
        }

        return ResponseUtil.success(mapToFinancialResponse(bidFinancial.get()), "Financial draft retrieved");
    }

    @Override
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> getBidDetails(Long bidTechnicalId) {
        log.info("Getting bid details: {}", bidTechnicalId);

        BidTechnical bid = bidTechnicalRepository.findById(bidTechnicalId)
                .orElseThrow(() -> new RuntimeException("Bid not found"));

        return ResponseUtil.success(mapToTechnicalResponse(bid), "Bid details retrieved");
    }

    @Override
    public ResponseEntity<ApiResponse<List<BidTechnicalResponse>>> getPendingTechnicalBids(Long tenderId) {
        log.info("Getting pending technical bids for tender: {}", tenderId);

        TenderHeader tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new RuntimeException("Tender not found"));

        List<BidTechnical> bids = bidTechnicalRepository.findByTenderAndSubmissionStatus(tender, "SUBMITTED");

        List<BidTechnicalResponse> responses = bids.stream()
                .map(this::mapToTechnicalResponse)
                .collect(Collectors.toList());

        return ResponseUtil.success(responses, "Pending technical bids retrieved");
    }

    @Override
    public ResponseEntity<ApiResponse<Boolean>> areAllVendorsEvaluated(Long tenderId) {
        TenderHeader tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new RuntimeException("Tender not found"));

        // ✅ Get all SUBMITTED bids (excluding DRAFT)
        List<BidTechnical> allBids = bidTechnicalRepository.findByTenderAndSubmissionStatus(tender, "SUBMITTED");

        // ✅ Filter out WITHDRAWN vendors - they don't need evaluation
        List<BidTechnical> activeBids = allBids.stream()
                .filter(b -> !"WITHDRAWN".equals(b.getEvaluationStatus()))
                .collect(Collectors.toList());

        log.info("Total bids: {}, Active bids (excluding withdrawn): {}", allBids.size(), activeBids.size());

        // ✅ If no active bids, consider as evaluated (tender may be cancelled)
        if (activeBids.isEmpty()) {
            log.warn("No active bids found for tender: {}. All vendors have withdrawn.", tenderId);
            return ResponseUtil.success(true, "No active vendors. Tender may need cancellation.");
        }

        // ✅ Check if all ACTIVE bids are evaluated (QUALIFIED or DISQUALIFIED)
        boolean allEvaluated = activeBids.stream()
                .allMatch(b -> "QUALIFIED".equals(b.getEvaluationStatus()) ||
                        "DISQUALIFIED".equals(b.getEvaluationStatus()));

        log.info("All active bids evaluated: {}", allEvaluated);

        return ResponseUtil.success(allEvaluated, allEvaluated ? "All vendors evaluated" : "Some vendors pending");
    }
    @Override
    public ResponseEntity<ApiResponse<Boolean>> hasVendorParticipated(Long tenderId) {
        try {
            Vendor vendor = getCurrentVendor();
            TenderHeader tender = tenderRepository.findById(tenderId)
                    .orElseThrow(() -> new RuntimeException("Tender not found"));

            Optional<BidTechnical> existing = bidTechnicalRepository.findByTenderAndVendor(tender, vendor);

            boolean hasParticipated = existing.isPresent() &&
                    "SUBMITTED".equals(existing.get().getSubmissionStatus());

            return ResponseUtil.success(hasParticipated, "Participation status retrieved");
        } catch (RuntimeException e) {
            return ResponseUtil.success(false, "Not participated");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<List<BidTechnicalResponse>>> getMyBids() {
        log.info("Getting all bids for logged-in vendor");

        Vendor vendor = getCurrentVendor();
        List<BidTechnical> bids = bidTechnicalRepository.findByVendor(vendor);

        List<BidTechnicalResponse> responses = bids.stream()
                .map(this::mapToTechnicalResponse)
                .collect(Collectors.toList());

        return ResponseUtil.success(responses, "Your bids retrieved");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> withdrawBid(Long bidTechnicalId, String reason) {
        log.info("Withdrawing bid: {} with reason: {}", bidTechnicalId, reason);

        BidTechnical bid = bidTechnicalRepository.findById(bidTechnicalId)
                .orElseThrow(() -> new RuntimeException("Bid not found"));

        Vendor vendor = getCurrentVendor();
        if (!bid.getVendor().getVendorId().equals(vendor.getVendorId())) {
            return ResponseUtil.error("You can only withdraw your own bids");
        }

        if (!"PENDING".equals(bid.getEvaluationStatus())) {
            return ResponseUtil.error("Cannot withdraw bid that is already evaluated");
        }

        bid.setEvaluationStatus("WITHDRAWN");
        bid.setEvaluationRemarks("Withdrawn by vendor. Reason: " + reason);

        BidTechnical saved = bidTechnicalRepository.save(bid);

        auditService.log("WITHDRAW_BID", "BidTechnical", bidTechnicalId,
                null, reason, "Bid withdrawn by vendor");

        return ResponseUtil.success(mapToTechnicalResponse(saved), "Bid withdrawn successfully");
    }

    // ==================== CLARIFICATION ====================

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> requestClarification(ClarificationRequest request) {
        log.info("Requesting clarification for bid: {}", request.getBidTechnicalId());

        BidTechnical bid = bidTechnicalRepository.findById(request.getBidTechnicalId())
                .orElseThrow(() -> new RuntimeException("Bid not found"));

        bid.setEvaluationStatus("CLARIFICATION_NEEDED");
        bid.setClarificationRequired("YES");
        bid.setClarificationQuestion(request.getQuestion());
        bid.setClarificationDeadline(request.getDeadline());
        bid.setResubmissionCount((bid.getResubmissionCount() == null ? 0 : bid.getResubmissionCount()) + 1);

        BidTechnical saved = bidTechnicalRepository.save(bid);

        auditService.log("REQUEST_CLARIFICATION", "BidTechnical", request.getBidTechnicalId(),
                null, request.getQuestion(), "Deadline: " + request.getDeadline());

        return ResponseUtil.success(mapToTechnicalResponse(saved), "Clarification requested");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> submitClarificationResponse(
            ClarificationResponse request, MultipartFile file) {
        log.info("Submitting clarification response for bid: {}", request.getBidTechnicalId());

        BidTechnical bid = bidTechnicalRepository.findById(request.getBidTechnicalId())
                .orElseThrow(() -> new RuntimeException("Bid not found"));

        if (bid.getClarificationDeadline() != null &&
                LocalDateTime.now().isAfter(bid.getClarificationDeadline())) {
            return ResponseUtil.error("Deadline has passed. Cannot submit response.");
        }

        bid.setVendorResponse(request.getResponse());
        bid.setRespondedAt(LocalDateTime.now());
        bid.setEvaluationStatus("PENDING");

        if (file != null && !file.isEmpty()) {
            String documentPath = saveClarificationDocument(file, bid);
            bid.setClarificationDocumentPath(documentPath);
        }

        BidTechnical saved = bidTechnicalRepository.save(bid);

        auditService.log("SUBMIT_CLARIFICATION", "BidTechnical", request.getBidTechnicalId(),
                null, request.getResponse(), null);

        return ResponseUtil.success(mapToTechnicalResponse(saved), "Clarification response submitted");
    }

    @Override
    public ResponseEntity<ApiResponse<List<BidTechnicalResponse>>> getPendingClarifications() {
        log.info("Getting pending clarifications for vendor");

        Vendor vendor = getCurrentVendor();

        List<BidTechnical> pendingClarifications = bidTechnicalRepository
                .findByVendorAndEvaluationStatus(vendor, "CLARIFICATION_NEEDED");

        List<BidTechnicalResponse> responses = pendingClarifications.stream()
                .map(this::mapToTechnicalResponse)
                .collect(Collectors.toList());

        return ResponseUtil.success(responses, "Pending clarifications retrieved");
    }

    @Override
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBidDocuments(Long bidTechnicalId) {
        log.info("Getting bid documents for technical bid: {}", bidTechnicalId);

        BidTechnical bid = bidTechnicalRepository.findById(bidTechnicalId)
                .orElseThrow(() -> new RuntimeException("Bid not found"));

        Map<String, Object> documents = new LinkedHashMap<>();

        // Helper to create document info with display name
        java.util.function.Function<String, Map<String, String>> createDocInfo = (path) -> {
            if (path == null) return null;
            File file = new File(path);
            String fileName = file.getName();
            // Remove timestamp prefix for display
            String displayName = fileName.replaceFirst("^\\d+_", "");
            return Map.of(
                    "fileName", fileName,
                    "displayName", displayName,
                    "filePath", path
            );
        };

        // Add documents with proper labels
        if (bid.getExperienceCertPath() != null) {
            documents.put("Experience Certificate", createDocInfo.apply(bid.getExperienceCertPath()));
        }
        if (bid.getOemAuthPath() != null) {
            documents.put("OEM Authorization Letter", createDocInfo.apply(bid.getOemAuthPath()));
        }
        if (bid.getGstCertPath() != null) {
            documents.put("GST Certificate", createDocInfo.apply(bid.getGstCertPath()));
        }
        if (bid.getPanCardPath() != null) {
            documents.put("PAN Card", createDocInfo.apply(bid.getPanCardPath()));
        }
        if (bid.getMsmeCertPath() != null) {
            documents.put("MSME Certificate", createDocInfo.apply(bid.getMsmeCertPath()));
        }
        if (bid.getOtherDocsPath() != null) {
            String[] paths = bid.getOtherDocsPath().split(",");
            List<Map<String, String>> otherDocs = new ArrayList<>();
            for (String path : paths) {
                otherDocs.add(createDocInfo.apply(path));
            }
            documents.put("Other Documents", otherDocs);
        }

        log.info("Returning {} document categories", documents.size());
        return ResponseUtil.success(documents, "Documents retrieved");
    }
    // Helper method to get display name from filename
    private String getDisplayName(String fileName) {
        if (fileName == null) return "Unknown";
        // Remove timestamp prefix if present (format: timestamp_filename)
        int underscoreIndex = fileName.indexOf('_');
        if (underscoreIndex > 0 && underscoreIndex < 15) {
            String namePart = fileName.substring(underscoreIndex + 1);
            if (namePart.length() > 0) return namePart;
        }
        return fileName;
    }
    // ==================== HELPER METHODS ====================

    private Vendor getCurrentVendor() {
        String username = CurrentUser.getCurrentUserOrThrow().getUsername();
        return vendorRepository.findByEmailId(username)
                .orElseThrow(() -> new RuntimeException("Vendor not found for user: " + username));
    }

    private TenderHeader getTenderAndValidate(Long tenderId) {
        TenderHeader tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new RuntimeException("Tender not found"));

        if (!"PUBLISHED".equals(tender.getTenderStatus())) {
            throw new RuntimeException("Tender is not published");
        }
        return tender;
    }

    private boolean isTenderAcceptingBids(TenderHeader tender) {
        LocalDate today = LocalDate.now();
        return tender.getBidStartDate() != null && tender.getBidEndDate() != null &&
                !today.isBefore(tender.getBidStartDate()) &&
                !today.isAfter(tender.getBidEndDate());
    }

    private String saveTechnicalDocument(MultipartFile file, Long bidTechnicalId) {
        try {
            if (bidTechnicalId == null) {
                log.error("bidTechnicalId is null, cannot save file");
                return null;
            }

            File dir = new File(UPLOAD_DIR + "/technical/" + bidTechnicalId);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename().replaceAll("\\s+", "_");
            File dest = new File(dir, fileName);
            file.transferTo(dest);
            return dest.getAbsolutePath();
        } catch (IOException e) {
            log.error("Error saving technical document: {}", e.getMessage());
            return null;
        }
    }

    private String saveFinancialDocumentFile(MultipartFile file, Long id) {
        try {
            File dir = new File(UPLOAD_DIR + "/financial/" + id);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename().replaceAll("\\s+", "_");
            File dest = new File(dir, fileName);
            file.transferTo(dest);
            return dest.getAbsolutePath();
        } catch (IOException e) {
            log.error("Error saving financial document: {}", e.getMessage());
            return null;
        }
    }

    private String saveClarificationDocument(MultipartFile file, BidTechnical bid) {
        try {
            File dir = new File(UPLOAD_DIR + "/clarification/" + bid.getBidTechnicalId());
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File dest = new File(dir, fileName);
            file.transferTo(dest);
            return dest.getAbsolutePath();
        } catch (IOException e) {
            log.error("Error saving clarification document: {}", e.getMessage());
            return null;
        }
    }

    private void updateTechnicalFields(BidTechnical bidTechnical, BidTechnicalRequest request) {
        bidTechnical.setCompanyName(request.getCompanyName());
        bidTechnical.setGstNumber(request.getGstNumber());
        bidTechnical.setPanNumber(request.getPanNumber());
        bidTechnical.setMakeIndiaClass(request.getMakeIndiaClass());

        if (request.getBidderTurnover() != null) {
            bidTechnical.setBidderTurnover(request.getBidderTurnover());
        }
        if (request.getOemTurnover() != null) {
            bidTechnical.setOemTurnover(request.getOemTurnover());
        }

        bidTechnical.setOemName(request.getOemName());
        bidTechnical.setAuthorizationDetails(request.getAuthorizationDetails());
        bidTechnical.setMsmeNumber(request.getMsmeNumber());
        bidTechnical.setIsMsme(request.getIsMsme() != null && request.getIsMsme());
    }

    private void updateTechnicalFields(BidTechnical bidTechnical, BidFinalSubmissionRequest request) {
        bidTechnical.setCompanyName(request.getCompanyName());
        bidTechnical.setGstNumber(request.getGstNumber());
        bidTechnical.setPanNumber(request.getPanNumber());
        bidTechnical.setMakeIndiaClass(request.getMakeIndiaClass());

        if (request.getBidderTurnover() != null) {
            bidTechnical.setBidderTurnover(request.getBidderTurnover());
        }
        if (request.getOemTurnover() != null) {
            bidTechnical.setOemTurnover(request.getOemTurnover());
        }

        bidTechnical.setOemName(request.getOemName());
        bidTechnical.setAuthorizationDetails(request.getAuthorizationDetails());
        bidTechnical.setMsmeNumber(request.getMsmeNumber());
        bidTechnical.setIsMsme(request.getIsMsme() != null && request.getIsMsme());
    }

    public BidTechnicalResponse mapToTechnicalResponse(BidTechnical entity) {
        BidTechnicalResponse response = new BidTechnicalResponse();
        response.setBidTechnicalId(entity.getBidTechnicalId());
        response.setTenderId(entity.getTender().getTenderId());
        response.setTenderNo(entity.getTender().getTenderNo());
        response.setTenderTitle(entity.getTender().getTenderTitle());
        response.setVendorId(entity.getVendor().getVendorId());
        response.setVendorName(entity.getVendor().getVendorName());
        response.setCompanyName(entity.getCompanyName());
        response.setGstNumber(entity.getGstNumber());
        response.setPanNumber(entity.getPanNumber());
        response.setMakeIndiaClass(entity.getMakeIndiaClass());
        response.setBidderTurnover(entity.getBidderTurnover());
        response.setOemTurnover(entity.getOemTurnover());
        response.setOemName(entity.getOemName());
        response.setAuthorizationDetails(entity.getAuthorizationDetails());
        response.setMsmeNumber(entity.getMsmeNumber());
        response.setIsMsme(entity.getIsMsme());
        response.setEvaluationStatus(entity.getEvaluationStatus());
        response.setEvaluationScore(entity.getEvaluationScore());
        response.setEvaluationRemarks(entity.getEvaluationRemarks());
        response.setSubmittedAt(entity.getSubmittedAt());
        response.setSubmissionStatus(entity.getSubmissionStatus());
        response.setClarificationQuestion(entity.getClarificationQuestion());
        response.setClarificationDeadline(entity.getClarificationDeadline());
        response.setVendorResponse(entity.getVendorResponse());

        // ✅ CRITICAL: Set document paths
        response.setExperienceCertPath(entity.getExperienceCertPath());
        response.setOemAuthPath(entity.getOemAuthPath());
        response.setGstCertPath(entity.getGstCertPath());
        response.setPanCardPath(entity.getPanCardPath());
        response.setMsmeCertPath(entity.getMsmeCertPath());
        response.setOtherDocsPath(entity.getOtherDocsPath());

        // ✅ Log for debugging
        log.info("Mapping technical response - ExpCert: {}, OemAuth: {}, GstCert: {}, PanCard: {}, MsmeCert: {}, OtherDocs: {}",
                entity.getExperienceCertPath(), entity.getOemAuthPath(), entity.getGstCertPath(),
                entity.getPanCardPath(), entity.getMsmeCertPath(), entity.getOtherDocsPath());

        return response;
    }
    private BidFinancialResponse mapToFinancialResponse(BidFinancial entity) {
        BidFinancialResponse response = new BidFinancialResponse();
        response.setBidFinancialId(entity.getBidFinancialId());
        response.setBidTechnicalId(entity.getBidTechnical().getBidTechnicalId());
        response.setTenderId(entity.getTender().getTenderId());
        response.setTenderNo(entity.getTender().getTenderNo());
        response.setVendorId(entity.getVendor().getVendorId());
        response.setVendorName(entity.getVendor().getVendorName());

        // ✅ ALWAYS decrypt for draft (isRevealed = "NO" means draft)
        // For final submitted bids, decrypt only if revealed
        try {
            response.setTotalBidAmount(encryptionUtil.decryptToBigDecimal(entity.getEncryptedTotalBidAmount()));
            response.setGstPercent(encryptionUtil.decryptToBigDecimal(entity.getEncryptedGstPercent()));
            response.setTotalCost(encryptionUtil.decryptToBigDecimal(entity.getEncryptedTotalCost()));
            response.setBankName(encryptionUtil.decrypt(entity.getEncryptedBankName()));
            response.setAccountNumber(encryptionUtil.decrypt(entity.getEncryptedAccountNumber()));
            response.setIfscCode(encryptionUtil.decrypt(entity.getEncryptedIfscCode()));
            response.setEmdNumber(encryptionUtil.decrypt(entity.getEncryptedEmdNumber()));
            response.setEmdValue(encryptionUtil.decryptToBigDecimal(entity.getEncryptedEmdValue()));

            response.setBoqFilePath(entity.getBoqFilePath());
            response.setPriceBreakupPath(entity.getPriceBreakupPath());
            response.setEmdReceiptPath(entity.getEmdReceiptPath());
            response.setOtherFinancialDocsPath(entity.getOtherFinancialDocsPath());
        } catch (Exception e) {
            log.error("Error decrypting financial data: {}", e.getMessage());
        }

        response.setEmdExemptionDetails(entity.getEmdExemptionDetails());
        response.setIsRevealed(entity.getIsRevealed());
        return response;
    }

    @Override
    public ResponseEntity<ApiResponse<List<BidFinancialResponse>>> getL1Vendors(Long tenderId) {
        log.info("Calculating L1/L2/L3 for tender: {}", tenderId);

        // Get all revealed financial bids (all ranks)
        List<BidFinancial> allRevealed = bidFinancialRepository.findRevealedFinancialsByTenderId(tenderId);

        // Filter out WITHDRAWN
        List<BidFinancial> validBids = allRevealed.stream()
                .filter(b -> b.getBidTechnical().getEvaluationStatus().equals("QUALIFIED"))
                .collect(Collectors.toList());

        if (validBids.isEmpty()) {
            return ResponseUtil.success(new ArrayList<>(), "No results available yet");
        }

        // Decrypt and sort by total cost (ascending)
        List<BidFinancialResponse> responses = validBids.stream()
                .map(this::mapToFinancialResponse)
                .sorted(Comparator.comparing(BidFinancialResponse::getTotalCost))
                .collect(Collectors.toList());

        // Calculate L1, L2, L3
        for (int i = 0; i < responses.size(); i++) {
            if (i == 0) responses.get(i).setIsRevealed("L1");
            else if (i == 1) responses.get(i).setIsRevealed("L2");
            else if (i == 2) responses.get(i).setIsRevealed("L3");
            else responses.get(i).setIsRevealed("L" + (i + 1));
        }

        return ResponseUtil.success(responses, "Rankings calculated successfully");
    }
}