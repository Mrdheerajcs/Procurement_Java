package com.procurement.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BidFinancialRequest {
    private Long tenderId;
    private Long bidTechnicalId;
    private BigDecimal totalBidAmount;
    private BigDecimal gstPercent;
    private BigDecimal totalCost;
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String emdNumber;
    private BigDecimal emdValue;
    private String emdExemptionDetails;
}