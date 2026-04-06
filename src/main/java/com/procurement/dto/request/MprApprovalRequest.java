package com.procurement.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MprApprovalRequest {
    private Long mprId;
    private String status;
    private String remarks;


}

