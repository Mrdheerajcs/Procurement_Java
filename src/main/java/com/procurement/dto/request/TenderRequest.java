package com.procurement.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
public class TenderRequest {
    private Long mprId;
    private String tenderTitle;
    private String tenderType;
    private LocalDate publishDate;
    private LocalDate closingDate;
    private LocalTime bidSubmissionEndTime;
    private BigDecimal emdAmount;
    private String tenderDescription;
}