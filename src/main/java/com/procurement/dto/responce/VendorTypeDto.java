package com.procurement.dto.responce;

import lombok.Data;

@Data
public class VendorTypeDto {
    private Long vendorTypeId;
    private String vendorTypeCode;
    private String vendorTypeName;
    private String status;
}