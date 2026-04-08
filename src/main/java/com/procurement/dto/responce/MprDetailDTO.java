package com.procurement.dto.responce;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MprDetailDTO {

    private Long mprDetailId;
    private Long mprId;
    private Integer slNo;

    private String itemCode;
    private String itemName;
    private String uom;
    private String specificationn;
    private BigDecimal requestedQty;
    private BigDecimal estimatedRate;
    private BigDecimal estimatedValue;
    private BigDecimal stockAvailable;
    private BigDecimal avgMonthlyConsumption;
    private String lastPurchaseInfo;
    private String remarks;
//    private String createdBy;
    private String updatedBy;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
   private LocalDateTime lastUpdatedDt;
    private LocalDateTime approvalDate;
    private String status;
    private List<Long> vendorIds;
    // Getters and Setters
}
