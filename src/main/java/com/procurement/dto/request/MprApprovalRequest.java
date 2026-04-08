package com.procurement.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class MprApprovalRequest {
    private Long mprHeaderId;
    private List<MprApprovalList> mprApprovalLists;



}

