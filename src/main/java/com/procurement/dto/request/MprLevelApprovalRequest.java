package com.procurement.dto.request;

import lombok.Data;

@Data
public class MprLevelApprovalRequest {
    private Long mprId;
    private String action;        // APPROVE, REJECT
    private String remarks;
}