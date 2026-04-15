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
    private LocalDate publishDate;
    private LocalDate closingDate;  // This maps to bid_end_date in DB
    private LocalTime bidSubmissionEndTime;
    private BigDecimal emdAmount;
    private String tenderDescription;
    private String documentPath;
}