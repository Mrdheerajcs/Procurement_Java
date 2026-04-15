package com.procurement.controller;

import com.procurement.dto.request.MprLevelApprovalRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.MprApprovalStatusResponse;
import com.procurement.service.MprLevelApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/mpr/approval-level")
@RequiredArgsConstructor
public class MprLevelApprovalController {

    private final MprLevelApprovalService approvalService;

    @PostMapping("/submit/{mprId}")
    public ResponseEntity<ApiResponse<String>> submitForApproval(@PathVariable Long mprId) {
        log.info("API: Submit MPR for approval: {}", mprId);
        return approvalService.submitForApproval(mprId);
    }

    @PutMapping("/approve")
    public ResponseEntity<ApiResponse<String>> approveAtLevel(@RequestBody MprLevelApprovalRequest request) {
        log.info("API: Approve MPR at level: {}", request.getMprId());
        return approvalService.approveAtLevel(request);
    }

    @PutMapping("/reject")
    public ResponseEntity<ApiResponse<String>> rejectAtLevel(@RequestBody MprLevelApprovalRequest request) {
        log.info("API: Reject MPR at level: {}", request.getMprId());
        return approvalService.rejectAtLevel(request);
    }

    @GetMapping("/status/{mprId}")
    public ResponseEntity<ApiResponse<MprApprovalStatusResponse>> getApprovalStatus(@PathVariable Long mprId) {
        log.info("API: Get approval status for MPR: {}", mprId);
        return approvalService.getApprovalStatus(mprId);
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<MprApprovalStatusResponse>>> getPendingApprovals(
            @RequestParam String role) {
        log.info("API: Get pending approvals for role: {}", role);
        return approvalService.getPendingApprovals(role);
    }
}