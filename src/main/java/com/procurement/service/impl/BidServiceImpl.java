package com.procurement.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.procurement.dto.request.BidFinancialRequest;
import com.procurement.dto.request.BidTechnicalRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.BidFinancialResponse;
import com.procurement.dto.responce.BidTechnicalResponse;
import com.procurement.entity.*;
import com.procurement.helper.CurrentUser;
import com.procurement.repository.*;
import com.procurement.service.AuditService;
import com.procurement.service.BidService;
import com.procurement.util.EncryptionUtil;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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

    private final String UPLOAD_DIR = "C:/uploads/bids/";

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> submitTechnicalBid(BidTechnicalRequest request, MultipartFile[] files) {
        log.info("Submitting technical bid for tender: {}", request.getTenderId());

        Vendor vendor = getCurrentVendor();
        TenderHeader tender = getTenderAndValidate(request.getTenderId());

        // Check if already submitted
        if (bidTechnicalRepository.existsByTenderAndVendor(tender, vendor)) {
            return ResponseUtil.error("Technical bid already submitted for this tender");
        }

        // Check if tender is accepting bids
        if (!isTenderAcceptingBids(tender)) {
            return ResponseUtil.error("Tender is not accepting bids at this time");
        }

        // Save technical bid
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
                .evaluationStatus("PENDING")
                .submittedAt(LocalDateTime.now())
                .build();

        // Save documents
        if (files != null) {
            for (MultipartFile file : files) {
                saveDocument(file, bidTechnical);
            }
        }

        BidTechnical saved = bidTechnicalRepository.save(bidTechnical);

        // Audit log
        auditService.log("SUBMIT_TECHNICAL_BID", "BidTechnical", saved.getBidTechnicalId(),
                null, objectMapper.valueToTree(saved).toString());

        return ResponseUtil.success(mapToTechnicalResponse(saved), "Technical bid submitted successfully");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<BidFinancialResponse>> submitFinancialBid(BidFinancialRequest request, MultipartFile[] files) {
        log.info("Submitting financial bid for tender: {}", request.getTenderId());

        Vendor vendor = getCurrentVendor();
        TenderHeader tender = getTenderAndValidate(request.getTenderId());

        // Verify technical bid exists and is qualified
        BidTechnical bidTechnical = bidTechnicalRepository.findById(request.getBidTechnicalId())
                .orElseThrow(() -> new RuntimeException("Technical bid not found"));

        if (!bidTechnical.getVendor().getVendorId().equals(vendor.getVendorId())) {
            return ResponseUtil.error("Unauthorized: This technical bid does not belong to you");
        }

        if (!"QUALIFIED".equals(bidTechnical.getEvaluationStatus())) {
            return ResponseUtil.error("Cannot submit financial bid: Technical bid is not qualified");
        }

        // Check if already submitted
        if (bidFinancialRepository.findByBidTechnical(bidTechnical).isPresent()) {
            return ResponseUtil.error("Financial bid already submitted");
        }

        // Encrypt financial data
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

        // Save BOQ file
        if (files != null && files.length > 0) {
            saveFinancialDocument(files[0], bidFinancial);
        }

        BidFinancial saved = bidFinancialRepository.save(bidFinancial);

        auditService.log("SUBMIT_FINANCIAL_BID", "BidFinancial", saved.getBidFinancialId(),
                null, "Financial bid submitted (encrypted)");

        return ResponseUtil.success(mapToFinancialResponse(saved), "Financial bid submitted successfully");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> evaluateTechnicalBid(
            Long bidTechnicalId, String status, Integer score, String remarks) {
        log.info("Evaluating technical bid: {} with status: {}", bidTechnicalId, status);

        BidTechnical bidTechnical = bidTechnicalRepository.findById(bidTechnicalId)
                .orElseThrow(() -> new RuntimeException("Technical bid not found"));

        String oldStatus = bidTechnical.getEvaluationStatus();

        // ✅ FIX: Update the status correctly
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

        // Verify current user is authorized (Tender Opening Officer)
        // This should be role-based check

        List<BidTechnical> qualifiedBids = bidTechnicalRepository.findQualifiedBidsByTenderId(tenderId);

        List<BidFinancialResponse> responses = new ArrayList<>();

        for (BidTechnical bidTech : qualifiedBids) {
            Optional<BidFinancial> optFinancial = bidFinancialRepository.findByBidTechnical(bidTech);
            if (optFinancial.isPresent() && "NO".equals(optFinancial.get().getIsRevealed())) {
                bidFinancialRepository.revealFinancial(bidTech.getBidTechnicalId(),
                        CurrentUser.getCurrentUserOrThrow().getUsername());

                BidFinancial financial = optFinancial.get();
                financial.setIsRevealed("YES");
                responses.add(mapToFinancialResponse(financial));
            }
        }

        auditService.log("REVEAL_FINANCIAL_BIDS", "TenderHeader", tenderId,
                null, "Revealed " + responses.size() + " financial bids");

        return ResponseUtil.success(responses, "Financial bids revealed successfully");
    }

    @Override
    public ResponseEntity<ApiResponse<List<BidFinancialResponse>>> getL1Vendors(Long tenderId) {
        log.info("Calculating L1 for tender: {}", tenderId);

        List<BidFinancial> revealedBids = bidFinancialRepository.findRevealedFinancialsByTenderId(tenderId);

        if (revealedBids.isEmpty()) {
            return ResponseUtil.success(new ArrayList<>(), "No revealed financial bids found");
        }

        // Decrypt and sort by total cost
        List<BidFinancialResponse> responses = revealedBids.stream()
                .map(this::mapToFinancialResponse)
                .sorted(Comparator.comparing(BidFinancialResponse::getTotalCost))
                .collect(Collectors.toList());

        // Mark L1
        if (!responses.isEmpty()) {
            responses.get(0).setIsRevealed("L1");
        }

        return ResponseUtil.success(responses, "L1 vendors calculated");
    }

    // ========== HELPER METHODS ==========

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

    private void saveDocument(MultipartFile file, BidTechnical bidTechnical) {
        try {
            File dir = new File(UPLOAD_DIR + "technical/" + bidTechnical.getBidTechnicalId());
            if (!dir.exists()) dir.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File dest = new File(dir, fileName);
            file.transferTo(dest);

            // Store path in appropriate field based on file type
            // Simplified: store all in technicalDocPath as comma separated
            if (bidTechnical.getTechnicalDocPath() == null) {
                bidTechnical.setTechnicalDocPath(dest.getAbsolutePath());
            } else {
                bidTechnical.setTechnicalDocPath(bidTechnical.getTechnicalDocPath() + "," + dest.getAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Error saving document: {}", e.getMessage());
        }
    }

    private void saveFinancialDocument(MultipartFile file, BidFinancial bidFinancial) {
        try {
            File dir = new File(UPLOAD_DIR + "financial/" + bidFinancial.getBidFinancialId());
            if (!dir.exists()) dir.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File dest = new File(dir, fileName);
            file.transferTo(dest);
            bidFinancial.setBoqFilePath(dest.getAbsolutePath());
        } catch (IOException e) {
            log.error("Error saving financial document: {}", e.getMessage());
        }
    }

    @Override
    public ResponseEntity<ApiResponse<List<BidTechnicalResponse>>> getTechnicalBidsByTender(Long tenderId, String status) {
        log.info("Fetching technical bids for tender: {} with status: {}", tenderId, status);

        TenderHeader tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new RuntimeException("Tender not found"));

        List<BidTechnical> bids;

        if (status != null && !status.isEmpty()) {
            bids = bidTechnicalRepository.findByTenderAndEvaluationStatus(tender, status);
        } else {
            bids = bidTechnicalRepository.findByTender(tender);
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

        List<BidFinancial> bids = bidFinancialRepository.findByTender(tender);

        List<BidFinancialResponse> responses = bids.stream()
                .map(this::mapToFinancialResponse)
                .collect(Collectors.toList());

        return ResponseUtil.success(responses, "Financial bids fetched successfully");
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

        // Decrypt if revealed
        if ("YES".equals(entity.getIsRevealed())) {
            response.setTotalBidAmount(encryptionUtil.decryptToBigDecimal(entity.getEncryptedTotalBidAmount()));
            response.setGstPercent(encryptionUtil.decryptToBigDecimal(entity.getEncryptedGstPercent()));
            response.setTotalCost(encryptionUtil.decryptToBigDecimal(entity.getEncryptedTotalCost()));
            response.setBankName(encryptionUtil.decrypt(entity.getEncryptedBankName()));
            response.setAccountNumber(encryptionUtil.decrypt(entity.getEncryptedAccountNumber()));
            response.setIfscCode(encryptionUtil.decrypt(entity.getEncryptedIfscCode()));
            response.setEmdNumber(encryptionUtil.decrypt(entity.getEncryptedEmdNumber()));
            response.setEmdValue(encryptionUtil.decryptToBigDecimal(entity.getEncryptedEmdValue()));
        }

        response.setEmdExemptionDetails(entity.getEmdExemptionDetails());
        response.setIsRevealed(entity.getIsRevealed());
        return response;
    }
}