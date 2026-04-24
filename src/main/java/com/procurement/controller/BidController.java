package com.procurement.controller;

import com.procurement.dto.request.BidFinalSubmissionRequest;
import com.procurement.dto.request.BidFinancialRequest;
import com.procurement.dto.request.BidTechnicalRequest;
import com.procurement.dto.request.ClarificationRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.BidFinancialResponse;
import com.procurement.dto.responce.BidTechnicalResponse;
import com.procurement.dto.responce.ClarificationResponse;
import com.procurement.entity.BidTechnical;
import com.procurement.repository.BidTechnicalRepository;
import com.procurement.service.BidService;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;
    private final BidTechnicalRepository bidTechnicalRepository;

    @PostMapping("/technical")
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> submitTechnicalBid(
            @RequestPart("data") BidTechnicalRequest request,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        log.info("API: Submit technical bid");
        return bidService.submitTechnicalBid(request, files);
    }

    @PostMapping("/financial")
    public ResponseEntity<ApiResponse<BidFinancialResponse>> submitFinancialBid(
            @RequestPart("data") BidFinancialRequest request,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        log.info("API: Submit financial bid");
        return bidService.submitFinancialBid(request, files);
    }

    @GetMapping("/technical/tender/{tenderId}")
    public ResponseEntity<ApiResponse<List<BidTechnicalResponse>>> getTechnicalBidsByTender(
            @PathVariable Long tenderId,
            @RequestParam(required = false) String status) {
        log.info("API: Get technical bids for tender: {}", tenderId);
        return bidService.getTechnicalBidsByTender(tenderId, status);
    }

    @GetMapping("/financial/tender/{tenderId}")
    public ResponseEntity<ApiResponse<List<BidFinancialResponse>>> getFinancialBidsByTender(@PathVariable Long tenderId) {
        log.info("API: Get financial bids for tender: {}", tenderId);
        return bidService.getFinancialBidsByTender(tenderId);
    }

    @PutMapping("/technical/{bidTechnicalId}/evaluate")
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> evaluateTechnicalBid(
            @PathVariable Long bidTechnicalId,
            @RequestParam String status,
            @RequestParam(required = false) Integer score,
            @RequestParam(required = false) String remarks) {
        log.info("API: Evaluate technical bid: {}", bidTechnicalId);
        return bidService.evaluateTechnicalBid(bidTechnicalId, status, score, remarks);
    }

    @PostMapping("/financial/reveal/{tenderId}")
    public ResponseEntity<ApiResponse<List<BidFinancialResponse>>> revealFinancialBids(@PathVariable Long tenderId) {
        log.info("API: Reveal financial bids for tender: {}", tenderId);
        return bidService.revealFinancialBids(tenderId);
    }

    @GetMapping("/l1/{tenderId}")
    public ResponseEntity<ApiResponse<List<BidFinancialResponse>>> getL1Vendors(@PathVariable Long tenderId) {
        log.info("API: Get L1 vendors for tender: {}", tenderId);
        return bidService.getL1Vendors(tenderId);
    }

    @GetMapping("/technical/{bidTechnicalId}")
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> getTechnicalBidById(@PathVariable Long bidTechnicalId) {
        log.info("Fetching technical bid by ID: {}", bidTechnicalId);
        BidTechnical bidTechnical = bidTechnicalRepository.findById(bidTechnicalId)
                .orElseThrow(() -> new RuntimeException("Technical bid not found"));
        return ResponseUtil.success(bidService.mapToTechnicalResponse(bidTechnical), "Technical bid retrieved");
    }

    @PostMapping("/technical/draft")
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> saveTechnicalDraft(
            @RequestPart("data") BidTechnicalRequest request,
            @RequestPart(value = "experienceCertificate", required = false) MultipartFile experienceCertificate,
            @RequestPart(value = "oemAuthorization", required = false) MultipartFile oemAuthorization,
            @RequestPart(value = "gstCertificate", required = false) MultipartFile gstCertificate,
            @RequestPart(value = "panCard", required = false) MultipartFile panCard,
            @RequestPart(value = "msmeCertificate", required = false) MultipartFile msmeCertificate,
            @RequestPart(value = "otherDocs", required = false) MultipartFile[] otherDocs) {

        log.info("API: Save technical bid as draft");

        List<MultipartFile> allFiles = new java.util.ArrayList<>();
        if (experienceCertificate != null) allFiles.add(experienceCertificate);
        if (oemAuthorization != null) allFiles.add(oemAuthorization);
        if (gstCertificate != null) allFiles.add(gstCertificate);
        if (panCard != null) allFiles.add(panCard);
        if (msmeCertificate != null) allFiles.add(msmeCertificate);
        if (otherDocs != null) {
            allFiles.addAll(java.util.Arrays.asList(otherDocs));
        }

        log.info("Received {} files for technical draft", allFiles.size());
        return bidService.saveTechnicalDraft(request, allFiles.toArray(new MultipartFile[0]));
    }

    @PostMapping("/financial/draft")
    public ResponseEntity<ApiResponse<BidFinancialResponse>> saveFinancialDraft(
            @RequestPart("data") BidFinancialRequest request,
            @RequestPart(value = "boqFile", required = false) MultipartFile boqFile,
            @RequestPart(value = "priceBreakup", required = false) MultipartFile priceBreakup,
            @RequestPart(value = "emdReceipt", required = false) MultipartFile emdReceipt,
            @RequestPart(value = "otherFinancialDocs", required = false) MultipartFile[] otherFinancialDocs) {

        log.info("API: Save financial draft");

        List<MultipartFile> allFiles = new ArrayList<>();
        if (boqFile != null) allFiles.add(boqFile);
        if (priceBreakup != null) allFiles.add(priceBreakup);
        if (emdReceipt != null) allFiles.add(emdReceipt);
        if (otherFinancialDocs != null) {
            allFiles.addAll(Arrays.asList(otherFinancialDocs));
        }

        log.info("Received {} files for financial draft", allFiles.size());
        return bidService.saveFinancialDraft(request, allFiles.toArray(new MultipartFile[0]));
    }

    @PostMapping("/final")
    public ResponseEntity<ApiResponse<BidFinancialResponse>> submitFinalBid(
            @RequestPart("data") BidFinalSubmissionRequest request,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        log.info("API: Submit final bid");
        return bidService.submitFinalBid(request, files);
    }

    @GetMapping("/technical/pending/{tenderId}")
    public ResponseEntity<ApiResponse<List<BidTechnicalResponse>>> getPendingTechnicalBids(@PathVariable Long tenderId) {
        log.info("API: Get pending technical bids for tender: {}", tenderId);
        return bidService.getPendingTechnicalBids(tenderId);
    }

    @GetMapping("/technical/all-evaluated/{tenderId}")
    public ResponseEntity<ApiResponse<Boolean>> areAllVendorsEvaluated(@PathVariable Long tenderId) {
        log.info("API: Check if all vendors evaluated for tender: {}", tenderId);
        return bidService.areAllVendorsEvaluated(tenderId);
    }

    @PostMapping("/technical/clarification/request")
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> requestClarification(@RequestBody ClarificationRequest request) {
        log.info("API: Request clarification");
        return bidService.requestClarification(request);
    }

    @PostMapping("/technical/clarification/response")
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> submitClarificationResponse(
            @RequestPart("data") ClarificationResponse request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        log.info("API: Submit clarification response");
        return bidService.submitClarificationResponse(request, file);
    }

    @GetMapping("/technical/details/{bidTechnicalId}")
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> getBidDetails(@PathVariable Long bidTechnicalId) {
        log.info("API: Get bid details");
        return bidService.getBidDetails(bidTechnicalId);
    }

    @GetMapping("/technical/draft/{tenderId}")
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> getTechnicalDraft(@PathVariable Long tenderId) {
        log.info("API: Get technical draft for tender: {}", tenderId);
        return bidService.getTechnicalDraft(tenderId);
    }

    @GetMapping("/financial/draft/{tenderId}")
    public ResponseEntity<ApiResponse<BidFinancialResponse>> getFinancialDraft(@PathVariable Long tenderId) {
        log.info("API: Get financial draft for tender: {}", tenderId);
        return bidService.getFinancialDraft(tenderId);
    }

    @GetMapping("/vendor/pending-clarifications")
    public ResponseEntity<ApiResponse<List<BidTechnicalResponse>>> getPendingClarifications() {
        log.info("API: Get pending clarifications for vendor");
        return bidService.getPendingClarifications();
    }

    @GetMapping("/check-participation/{tenderId}")
    public ResponseEntity<ApiResponse<Boolean>> hasVendorParticipated(@PathVariable Long tenderId) {
        log.info("API: Check if vendor participated in tender: {}", tenderId);
        return bidService.hasVendorParticipated(tenderId);
    }

    @PutMapping("/withdraw/{bidTechnicalId}")
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> withdrawBid(
            @PathVariable Long bidTechnicalId,
            @RequestParam String reason) {
        log.info("API: Withdraw bid: {}", bidTechnicalId);
        return bidService.withdrawBid(bidTechnicalId, reason);
    }

    @GetMapping("/my-bids")
    public ResponseEntity<ApiResponse<List<BidTechnicalResponse>>> getMyBids() {
        log.info("API: Get my bids");
        return bidService.getMyBids();
    }

    @GetMapping("/technical/documents/{bidTechnicalId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBidDocuments(@PathVariable Long bidTechnicalId) {
        log.info("API: Get bid documents for technical bid: {}", bidTechnicalId);
        return bidService.getBidDocuments(bidTechnicalId);
    }


    @GetMapping("/technical/all")
    public ResponseEntity<ApiResponse<List<BidTechnicalResponse>>> getAllTechnicalBids() {
        log.info("API: Get all technical bids");
        List<BidTechnical> bids = bidTechnicalRepository.findAll();
        List<BidTechnicalResponse> responses = bids.stream()
                .map(bidService::mapToTechnicalResponse)
                .collect(Collectors.toList());
        return ResponseUtil.success(responses, "All bids retrieved");
    }
}