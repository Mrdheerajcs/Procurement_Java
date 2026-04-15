package com.procurement.dto.responce;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BidFinancialResponse {
    private Long bidFinancialId;
    private Long bidTechnicalId;
    private Long tenderId;
    private String tenderNo;
    private Long vendorId;
    private String vendorName;
    private BigDecimal totalBidAmount;
    private BigDecimal gstPercent;
    private BigDecimal totalCost;
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String emdNumber;
    private BigDecimal emdValue;
    private String emdExemptionDetails;
    private String isRevealed;
}