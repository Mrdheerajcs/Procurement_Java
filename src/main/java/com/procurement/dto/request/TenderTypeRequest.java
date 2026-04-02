package com.procurement.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenderTypeRequest {
    private Long tenderTypeId;
    private String tenderCode;
    private String tenderName;
}