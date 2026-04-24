package com.procurement.service.impl;

import com.procurement.dto.request.MprLevelApprovalRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.MprApprovalStatusResponse;
import com.procurement.entity.MprApprovalHistory;
import com.procurement.entity.MprHeader;
import com.procurement.helper.CurrentUser;
import com.procurement.repository.MprApprovalHistoryRepository;
import com.procurement.repository.MprRepository;
import com.procurement.service.AuditService;
import com.procurement.service.MprLevelApprovalService;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MprLevelApprovalServiceImpl implements MprLevelApprovalService {

    private final MprRepository mprRepository;
    private final MprApprovalHistoryRepository historyRepository;
    private final AuditService auditService;

    private static final String[] APPROVAL_LEVELS = {"MANAGER", "FINANCE", "DIRECTOR"};
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<String>> submitForApproval(Long mprId) {
        log.info("Submitting MPR {} for level 1 approval", mprId);

        MprHeader mpr = mprRepository.findById(mprId)
                .orElseThrow(() -> new RuntimeException("MPR not found"));

        String currentUser = CurrentUser.getCurrentUserOrThrow().getUsername();

        // Check if user is the creator
        if (!currentUser.equals(mpr.getCreatedBy())) {
            return ResponseUtil.error("Only the creator can submit MPR for approval");
        }

        // Check current status
        if (mpr.getApprovalStatus() != null && !"DRAFT".equals(mpr.getApprovalStatus()) && !"REJECTED".equals(mpr.getApprovalStatus())) {
            return ResponseUtil.error("MPR already submitted for approval");
        }

        // Set initial approval status
        mpr.setApprovalStatus(STATUS_PENDING);
        mpr.setApprovalLevel("MANAGER");
        mpr.setStatus("PENDING_APPROVAL");  // ✅ Set status for pending approval
        mprRepository.save(mpr);

        // Save history
        saveHistory(mpr, "SUBMITTED", currentUser, "Submitted for Manager approval");

        auditService.log("SUBMIT_MPR_APPROVAL", "MprHeader", mprId,
                "DRAFT", "PENDING_MANAGER", "Submitted for approval");

        return ResponseUtil.success("MPR submitted for Manager approval", "Success");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<String>> approveAtLevel(MprLevelApprovalRequest request) {
        log.info("Approving MPR {} at level", request.getMprId());

        MprHeader mpr = mprRepository.findById(request.getMprId())
                .orElseThrow(() -> new RuntimeException("MPR not found"));

        String currentUser = CurrentUser.getCurrentUserOrThrow().getUsername();
        String currentRole = getCurrentUserRole();

        // Verify user has correct role for current approval level
        if (!canApproveAtLevel(mpr, currentRole)) {
            return ResponseUtil.error("You are not authorized to approve at this level. Required: " + getRoleForLevel(mpr.getApprovalLevel()));
        }

        // Update based on current level
        String currentLevel = mpr.getApprovalLevel();

        switch (currentLevel) {
            case "MANAGER":
                mpr.setApprovedByLevel1(currentUser);
                mpr.setApprovedAtLevel1(LocalDateTime.now());
                mpr.setRemarksLevel1(request.getRemarks());
                mpr.setApprovalLevel("FINANCE");
                saveHistory(mpr, "APPROVED", currentUser, request.getRemarks());
                break;

            case "FINANCE":
                mpr.setApprovedByLevel2(currentUser);
                mpr.setApprovedAtLevel2(LocalDateTime.now());
                mpr.setRemarksLevel2(request.getRemarks());
                mpr.setApprovalLevel("DIRECTOR");
                saveHistory(mpr, "APPROVED", currentUser, request.getRemarks());
                break;

            case "DIRECTOR":
                mpr.setApprovedByLevel3(currentUser);
                mpr.setApprovedAtLevel3(LocalDateTime.now());
                mpr.setRemarksLevel3(request.getRemarks());
                mpr.setApprovalStatus(STATUS_APPROVED);
                mpr.setApprovalLevel("COMPLETED");
                mpr.setStatus("a");  // ✅ Final approved status for tender creation
                saveHistory(mpr, "APPROVED", currentUser, request.getRemarks());
                break;

            default:
                return ResponseUtil.error("Invalid approval level: " + currentLevel);
        }

        mprRepository.save(mpr);

        String message = currentLevel.equals("DIRECTOR") ?
                "MPR fully approved! Ready for tender creation." :
                "MPR approved at " + currentLevel + " level. Next: " + mpr.getApprovalLevel();

        auditService.log("APPROVE_MPR_LEVEL", "MprHeader", request.getMprId(),
                currentLevel, mpr.getApprovalLevel(), request.getRemarks());

        return ResponseUtil.success(message, "Success");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<String>> rejectAtLevel(MprLevelApprovalRequest request) {
        log.info("Rejecting MPR {} at level", request.getMprId());

        MprHeader mpr = mprRepository.findById(request.getMprId())
                .orElseThrow(() -> new RuntimeException("MPR not found"));

        String currentUser = CurrentUser.getCurrentUserOrThrow().getUsername();
        String currentRole = getCurrentUserRole();

        // Verify user has correct role
        if (!canApproveAtLevel(mpr, currentRole)) {
            return ResponseUtil.error("You are not authorized to reject at this level");
        }

        // Set rejection status
        mpr.setApprovalStatus(STATUS_REJECTED);
        mpr.setRejectionReason(request.getRemarks());
        mpr.setRejectedBy(currentUser);
        mpr.setRejectedAt(LocalDateTime.now());
        mpr.setStatus("r");  // Rejected status

        mprRepository.save(mpr);

        saveHistory(mpr, "REJECTED", currentUser, request.getRemarks());

        auditService.log("REJECT_MPR_LEVEL", "MprHeader", request.getMprId(),
                mpr.getApprovalLevel(), "REJECTED", request.getRemarks());

        return ResponseUtil.success("MPR rejected at " + mpr.getApprovalLevel() + " level", "Success");
    }

    @Override
    public ResponseEntity<ApiResponse<MprApprovalStatusResponse>> getApprovalStatus(Long mprId) {
        log.info("Getting approval status for MPR: {}", mprId);

        MprHeader mpr = mprRepository.findById(mprId)
                .orElseThrow(() -> new RuntimeException("MPR not found"));

        MprApprovalStatusResponse response = new MprApprovalStatusResponse();
        response.setMprId(mpr.getMprId());
        response.setMprNo(mpr.getMprNo());
        response.setDepartmentName(mpr.getDepartment() != null ? mpr.getDepartment().getDepartmentName() : null);
        response.setProjectName(mpr.getProjectName());

        // Set current status
        if (STATUS_APPROVED.equals(mpr.getApprovalStatus())) {
            response.setCurrentLevel("COMPLETED");
            response.setCurrentStatus("APPROVED");
        } else if (STATUS_REJECTED.equals(mpr.getApprovalStatus())) {
            response.setCurrentLevel(mpr.getApprovalLevel());
            response.setCurrentStatus("REJECTED");
            response.setRejectionReason(mpr.getRejectionReason());
        } else {
            response.setCurrentLevel(mpr.getApprovalLevel());
            response.setCurrentStatus(STATUS_PENDING);
        }

        // Level 1 (Manager)
        MprApprovalStatusResponse.LevelApprovalDetail level1 = new MprApprovalStatusResponse.LevelApprovalDetail();
        level1.setStatus(mpr.getApprovedByLevel1() != null ? "APPROVED" :
                (mpr.getApprovalLevel().equals("MANAGER") && STATUS_PENDING.equals(mpr.getApprovalStatus()) ? "PENDING" : "PENDING"));
        level1.setApprovedBy(mpr.getApprovedByLevel1());
        level1.setApprovedAt(mpr.getApprovedAtLevel1());
        level1.setRemarks(mpr.getRemarksLevel1());
        response.setLevel1(level1);

        // Level 2 (Finance)
        MprApprovalStatusResponse.LevelApprovalDetail level2 = new MprApprovalStatusResponse.LevelApprovalDetail();
        level2.setStatus(mpr.getApprovedByLevel2() != null ? "APPROVED" :
                (mpr.getApprovalLevel().equals("FINANCE") && STATUS_PENDING.equals(mpr.getApprovalStatus()) ? "PENDING" : "PENDING"));
        level2.setApprovedBy(mpr.getApprovedByLevel2());
        level2.setApprovedAt(mpr.getApprovedAtLevel2());
        level2.setRemarks(mpr.getRemarksLevel2());
        response.setLevel2(level2);

        // Level 3 (Director)
        MprApprovalStatusResponse.LevelApprovalDetail level3 = new MprApprovalStatusResponse.LevelApprovalDetail();
        level3.setStatus(mpr.getApprovedByLevel3() != null ? "APPROVED" :
                (mpr.getApprovalLevel().equals("DIRECTOR") && STATUS_PENDING.equals(mpr.getApprovalStatus()) ? "PENDING" : "PENDING"));
        level3.setApprovedBy(mpr.getApprovedByLevel3());
        level3.setApprovedAt(mpr.getApprovedAtLevel3());
        level3.setRemarks(mpr.getRemarksLevel3());
        response.setLevel3(level3);

        return ResponseUtil.success(response, "Approval status retrieved");
    }

    @Override
    public ResponseEntity<ApiResponse<List<MprApprovalStatusResponse>>> getPendingApprovals(String role) {
        log.info("Getting pending approvals for role: {}", role);

        String level = getLevelForRole(role);
        if (level == null) {
            log.warn("No level found for role: {}", role);
            return ResponseUtil.success(new ArrayList<>(), "No pending approvals for this role");
        }

        // Fetch MPRs that are pending at this level
        List<MprHeader> pendingMprs = mprRepository.findByApprovalStatusAndApprovalLevel(STATUS_PENDING, level);

        log.info("Found {} pending MPRs for level: {}", pendingMprs.size(), level);

        List<MprApprovalStatusResponse> responses = new ArrayList<>();
        for (MprHeader mpr : pendingMprs) {
            ResponseEntity<ApiResponse<MprApprovalStatusResponse>> statusResp = getApprovalStatus(mpr.getMprId());
            if (statusResp.getBody() != null && statusResp.getBody().getData() != null) {
                responses.add(statusResp.getBody().getData());
            }
        }

        return ResponseUtil.success(responses, "Pending approvals retrieved");
    }

    // ========== HELPER METHODS ==========

    private void saveHistory(MprHeader mpr, String action, String actionBy, String remarks) {
        MprApprovalHistory history = MprApprovalHistory.builder()
                .mprHeader(mpr)
                .approvalLevel(mpr.getApprovalLevel())
                .action(action)
                .actionBy(actionBy)
                .remarks(remarks)
                .actionAt(LocalDateTime.now())
                .build();
        historyRepository.save(history);
    }

    private boolean canApproveAtLevel(MprHeader mpr, String userRole) {
        String requiredLevel = mpr.getApprovalLevel();

        // If MPR is already fully approved or rejected, don't allow further action
        if ("COMPLETED".equals(requiredLevel) || STATUS_REJECTED.equals(mpr.getApprovalStatus())) {
            return false;
        }

        String requiredRole = getRoleForLevel(requiredLevel);
        return requiredRole != null && requiredRole.equals(userRole);
    }

    private String getRoleForLevel(String level) {
        switch (level) {
            case "MANAGER": return "ROLE_MANAGER";
            case "FINANCE": return "ROLE_FINANCE";
            case "DIRECTOR": return "ROLE_DIRECTOR";
            default: return null;
        }
    }

    private String getLevelForRole(String role) {
        switch (role) {
            case "ROLE_MANAGER": return "MANAGER";
            case "ROLE_FINANCE": return "FINANCE";
            case "ROLE_DIRECTOR": return "DIRECTOR";
            default: return null;
        }
    }

    // ✅ FIXED: Get current user role from Security Context
    private String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(role -> role.startsWith("ROLE_"))
                    .findFirst()
                    .orElse("ROLE_USER");
        }
        return "ROLE_USER";
    }
}