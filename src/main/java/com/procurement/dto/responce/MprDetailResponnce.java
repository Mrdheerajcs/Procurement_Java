package com.procurement.dto.responce;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class MprDetailResponnce {
    private long mprDetailId;
    private Integer slNo;
    private String itemCode;
    private String itemName;
    private String uom;
    private String specification; // corrected name
    private BigDecimal requestedQty;
    private BigDecimal estimatedRate;
    private BigDecimal estimatedValue;
    private BigDecimal stockAvailable;
    private BigDecimal avgMonthlyConsumption;
    private String lastPurchaseInfo;
    private String remarks;
    private String status;
    private List<VendorDTORes> vendors;
}


