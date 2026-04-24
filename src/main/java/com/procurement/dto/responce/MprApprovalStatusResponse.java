package com.procurement.dto.responce;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MprApprovalStatusResponse {
    private Long mprId;
    private String mprNo;
    private String departmentName;
    private String projectName;
    private String priority;
    private String currentLevel;     // MANAGER, FINANCE, DIRECTOR, COMPLETED
    private String currentStatus;    // PENDING, APPROVED, REJECTED
    private String rejectionReason;
    private LevelApprovalDetail level1;  // Manager
    private LevelApprovalDetail level2;  // Finance
    private LevelApprovalDetail level3;  // Director

    @Data
    public static class LevelApprovalDetail {
        private String status;        // PENDING, APPROVED, REJECTED
        private String approvedBy;
        private LocalDateTime approvedAt;
        private String remarks;
    }
}