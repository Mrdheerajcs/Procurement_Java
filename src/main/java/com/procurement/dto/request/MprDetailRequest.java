package com.procurement.dto.request;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class MprDetailRequest {
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
    private List<Long> vendorIds;
}
