package com.procurement.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MprApprovalList {
    private Long mprdetailId;
    private String status;
    private String remarks;
}
