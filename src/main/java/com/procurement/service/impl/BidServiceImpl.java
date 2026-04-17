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

    private final String UPLOAD_DIR = "C:/uploads/bids/";

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

        if (files != null) {
            for (MultipartFile file : files) {
                saveDocument(file, bidTechnical);
            }
        }

        BidTechnical saved = bidTechnicalRepository.save(bidTechnical);

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

        List<BidFinancialResponse> responses = revealedBids.stream()
                .map(this::mapToFinancialResponse)
                .sorted(Comparator.comparing(BidFinancialResponse::getTotalCost))
                .collect(Collectors.toList());

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

        List<BidTechnical> allBids = bidTechnicalRepository.findByTenderAndSubmissionStatus(tender, "SUBMITTED");

        boolean allEvaluated = allBids.stream()
                .allMatch(b -> "QUALIFIED".equals(b.getEvaluationStatus()) || "DISQUALIFIED".equals(b.getEvaluationStatus()));

        if (!allEvaluated) {
            log.info("Not all vendors evaluated yet. Cannot fetch financial bids.");
            return ResponseUtil.success(new ArrayList<>(), "Technical evaluation not complete yet. Please evaluate all vendors first.");
        }

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
        response.setSubmissionStatus(entity.getSubmissionStatus());
        response.setClarificationQuestion(entity.getClarificationQuestion());
        response.setClarificationDeadline(entity.getClarificationDeadline());
        response.setVendorResponse(entity.getVendorResponse());
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


    @Override
    @Transactional
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> saveTechnicalDraft(BidTechnicalRequest request, MultipartFile[] files) {
        log.info("Saving technical bid as DRAFT for tender: {}", request.getTenderId());
        log.info("Bidder Turnover received: {}", request.getBidderTurnover());
        log.info("OEM Turnover received: {}", request.getOemTurnover());

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
        } else {
            bidTechnical = BidTechnical.builder()
                    .tender(tender)
                    .vendor(vendor)
                    .submissionStatus("DRAFT")
                    .build();
            updateTechnicalFields(bidTechnical, request);
        }

        if (files != null) {
            for (MultipartFile file : files) {
                saveDocument(file, bidTechnical);
            }
        }

        BidTechnical saved = bidTechnicalRepository.save(bidTechnical);
        log.info("Saved Bidder Turnover: {}", saved.getBidderTurnover());
        log.info("Saved OEM Turnover: {}", saved.getOemTurnover());

        return ResponseUtil.success(mapToTechnicalResponse(saved), "Technical bid saved as draft");
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

        bidTechnicalRepository.save(bidTechnical);

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

        if (files != null && files.length > 0) {
            saveFinancialDocument(files[0], bidFinancial);
        }

        BidFinancial saved = bidFinancialRepository.save(bidFinancial);

        auditService.log("SUBMIT_FINAL_BID", "BidFinancial", saved.getBidFinancialId(),
                null, "Final bid submitted");

        return ResponseUtil.success(mapToFinancialResponse(saved), "Bid submitted successfully!");
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

        List<BidTechnical> bids = bidTechnicalRepository.findByTenderAndSubmissionStatus(tender, "SUBMITTED");

        boolean allEvaluated = bids.stream()
                .allMatch(b -> "QUALIFIED".equals(b.getEvaluationStatus()) || "DISQUALIFIED".equals(b.getEvaluationStatus()));

        return ResponseUtil.success(allEvaluated, allEvaluated ? "All vendors evaluated" : "Some vendors pending");
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
            ClarificationResponse request,
            MultipartFile file) {
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

        // ✅ Save document if uploaded
        if (file != null && !file.isEmpty()) {
            String documentPath = saveClarificationDocument(file, bid);
            bid.setClarificationDocumentPath(documentPath);
        }

        BidTechnical saved = bidTechnicalRepository.save(bid);

        auditService.log("SUBMIT_CLARIFICATION", "BidTechnical", request.getBidTechnicalId(),
                null, request.getResponse(), null);

        return ResponseUtil.success(mapToTechnicalResponse(saved), "Clarification response submitted");
    }

    private String saveClarificationDocument(MultipartFile file, BidTechnical bid) {
        try {
            File dir = new File(UPLOAD_DIR + "clarification/" + bid.getBidTechnicalId());
            if (!dir.exists()) dir.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File dest = new File(dir, fileName);
            file.transferTo(dest);
            return dest.getAbsolutePath();
        } catch (IOException e) {
            log.error("Error saving clarification document: {}", e.getMessage());
            return null;
        }
    }
    @Override
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> getBidDetails(Long bidTechnicalId) {
        log.info("Getting bid details: {}", bidTechnicalId);

        BidTechnical bid = bidTechnicalRepository.findById(bidTechnicalId)
                .orElseThrow(() -> new RuntimeException("Bid not found"));

        return ResponseUtil.success(mapToTechnicalResponse(bid), "Bid details retrieved");
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
    public ResponseEntity<ApiResponse<Boolean>> hasVendorParticipated(Long tenderId) {
        try {
            Vendor vendor = getCurrentVendor();
            TenderHeader tender = tenderRepository.findById(tenderId)
                    .orElseThrow(() -> new RuntimeException("Tender not found"));

            Optional<BidTechnical> existing = bidTechnicalRepository.findByTenderAndVendor(tender, vendor);

            // ✅ Only return true if FINAL submitted (not just draft)
            boolean hasParticipated = existing.isPresent() &&
                    "SUBMITTED".equals(existing.get().getSubmissionStatus());

            return ResponseUtil.success(hasParticipated, "Participation status retrieved");
        } catch (RuntimeException e) {
            return ResponseUtil.success(false, "Not participated");
        }
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
}