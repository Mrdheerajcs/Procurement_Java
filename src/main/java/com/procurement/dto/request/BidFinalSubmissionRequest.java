package com.procurement.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BidFinalSubmissionRequest {
    private Long tenderId;
    // Technical data
    private String companyName;
    private String gstNumber;
    private String panNumber;
    private String makeIndiaClass;
    private BigDecimal bidderTurnover;
    private BigDecimal oemTurnover;
    private String oemName;
    private String authorizationDetails;
    private String msmeNumber;
    private Boolean isMsme;
    // Financial data
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