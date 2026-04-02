package com.procurement.dto.responce;

import lombok.Data;

@Data
public class TenderTypeDto {
    private Long tenderTypeId;
    private String tenderCode;
    private String tenderName;
    private String status;
}