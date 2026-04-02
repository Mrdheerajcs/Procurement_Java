package com.procurement.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorTypeRequest {
    private Long vendorTypeId;
    private String vendorTypeCode;
    private String vendorTypeName;
}