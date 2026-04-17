package com.procurement.service;

import com.procurement.dto.request.BidFinalSubmissionRequest;
import com.procurement.dto.request.BidFinancialRequest;
import com.procurement.dto.request.BidTechnicalRequest;
import com.procurement.dto.request.ClarificationRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.BidFinancialResponse;
import com.procurement.dto.responce.BidTechnicalResponse;
import com.procurement.dto.responce.ClarificationResponse;
import com.procurement.entity.BidTechnical;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BidService {
    ResponseEntity<ApiResponse<BidTechnicalResponse>> submitTechnicalBid(BidTechnicalRequest request, MultipartFile[] files);
    ResponseEntity<ApiResponse<BidFinancialResponse>> submitFinancialBid(BidFinancialRequest request, MultipartFile[] files);
    ResponseEntity<ApiResponse<BidTechnicalResponse>> evaluateTechnicalBid(Long bidTechnicalId, String status, Integer score, String remarks);
    ResponseEntity<ApiResponse<List<BidFinancialResponse>>> revealFinancialBids(Long tenderId);
    ResponseEntity<ApiResponse<List<BidFinancialResponse>>> getL1Vendors(Long tenderId);
    ResponseEntity<ApiResponse<List<BidTechnicalResponse>>> getTechnicalBidsByTender(Long tenderId, String status);
    ResponseEntity<ApiResponse<List<BidFinancialResponse>>> getFinancialBidsByTender(Long tenderId);
    BidTechnicalResponse mapToTechnicalResponse(BidTechnical entity);

    // Draft & Final
    ResponseEntity<ApiResponse<BidTechnicalResponse>> saveTechnicalDraft(BidTechnicalRequest request, MultipartFile[] files);
    ResponseEntity<ApiResponse<BidFinancialResponse>> submitFinalBid(BidFinalSubmissionRequest request, MultipartFile[] files);
    ResponseEntity<ApiResponse<List<BidTechnicalResponse>>> getPendingTechnicalBids(Long tenderId);
    ResponseEntity<ApiResponse<Boolean>> areAllVendorsEvaluated(Long tenderId);
    ResponseEntity<ApiResponse<BidTechnicalResponse>> getTechnicalDraft(Long tenderId);

    ResponseEntity<ApiResponse<BidTechnicalResponse>> requestClarification(ClarificationRequest request);

    @Transactional
    ResponseEntity<ApiResponse<BidTechnicalResponse>> submitClarificationResponse(
            ClarificationResponse request,
            MultipartFile file);

    ResponseEntity<ApiResponse<BidTechnicalResponse>> getBidDetails(Long bidTechnicalId);
    ResponseEntity<ApiResponse<List<BidTechnicalResponse>>> getPendingClarifications();

    ResponseEntity<ApiResponse<Boolean>> hasVendorParticipated(Long tenderId);


    ResponseEntity<ApiResponse<List<BidTechnicalResponse>>> getMyBids();
    ResponseEntity<ApiResponse<BidTechnicalResponse>> withdrawBid(Long bidTechnicalId, String reason);
}