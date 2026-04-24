package com.procurement.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TenderRequest {
    private Long mprId;
    private String tenderNo;
    private String tenderTitle;
    private String tenderType;
    private String department;           // ✅ ADDED
    private String projectName;          // ✅ ADDED
    private String priority;             // ✅ ADDED
    private String bidType;              // NEW - Single Bid / Two Bid
    private String boqType;              // NEW - Item Rate / Lump Sum
    private BigDecimal estimatedValue;   // NEW
    private BigDecimal emdAmount;
    private BigDecimal tenderFee;        // NEW
    private Integer bidValidity;         // NEW
    private LocalDate publishDate;
    private LocalDate closingDate;
    private LocalTime bidSubmissionEndTime;
    private LocalDate preBidDate;        // NEW - Pre-bid meeting date
    private LocalDate techBidOpenDate;   // NEW - Technical bid opening date
    private LocalDate finBidOpenDate;    // NEW - Financial bid opening date
    private String description;
}