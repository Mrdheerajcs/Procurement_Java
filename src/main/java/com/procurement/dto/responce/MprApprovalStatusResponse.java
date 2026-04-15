package com.procurement.dto.responce;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MprApprovalStatusResponse {
    private Long mprId;
    private String mprNo;
    private String currentLevel;     // MANAGER, FINANCE, DIRECTOR, COMPLETED
    private String currentStatus;    // PENDING, APPROVED, REJECTED
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