package com.procurement.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContractRequest {
    private String contractNo;
    private Long tenderId;
    private String tenderNo;
    private String tenderTitle;
    private Long vendorId;
    private String vendorName;
    private LocalDate awardDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal amount;
    private String status;
}