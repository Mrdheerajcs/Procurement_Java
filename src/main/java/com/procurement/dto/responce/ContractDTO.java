package com.procurement.dto.responce;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ContractDTO {
    private Long contractId;
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
    private String pbgPath;
    private String createdBy;
    private LocalDateTime createdAt;
}