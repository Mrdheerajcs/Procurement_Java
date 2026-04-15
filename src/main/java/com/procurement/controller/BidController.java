package com.procurement.controller;


import com.procurement.dto.request.BidFinancialRequest;
import com.procurement.dto.request.BidTechnicalRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.BidFinancialResponse;
import com.procurement.dto.responce.BidTechnicalResponse;
import com.procurement.entity.BidTechnical;
import com.procurement.repository.BidTechnicalRepository;
import com.procurement.service.BidService;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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


    // ✅ GET single technical bid by ID
    @GetMapping("/technical/{bidTechnicalId}")
    public ResponseEntity<ApiResponse<BidTechnicalResponse>> getTechnicalBidById(@PathVariable Long bidTechnicalId) {
        log.info("Fetching technical bid by ID: {}", bidTechnicalId);
        BidTechnical bidTechnical = bidTechnicalRepository.findById(bidTechnicalId)
                .orElseThrow(() -> new RuntimeException("Technical bid not found"));
        return ResponseUtil.success(bidService.mapToTechnicalResponse(bidTechnical), "Technical bid retrieved");
    }
}