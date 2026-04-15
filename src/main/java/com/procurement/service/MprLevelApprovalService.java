package com.procurement.service;

import com.procurement.dto.request.MprLevelApprovalRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.MprApprovalStatusResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface MprLevelApprovalService {

    // Submit MPR for level 1 approval (after creation)
    ResponseEntity<ApiResponse<String>> submitForApproval(Long mprId);

    // Approve at current level (Manager/Finance/Director)
    ResponseEntity<ApiResponse<String>> approveAtLevel(MprLevelApprovalRequest request);

    // Reject at current level
    ResponseEntity<ApiResponse<String>> rejectAtLevel(MprLevelApprovalRequest request);

    // Get approval status of an MPR
    ResponseEntity<ApiResponse<MprApprovalStatusResponse>> getApprovalStatus(Long mprId);

    // Get pending approvals for a user based on role
    ResponseEntity<ApiResponse<List<MprApprovalStatusResponse>>> getPendingApprovals(String role);
}