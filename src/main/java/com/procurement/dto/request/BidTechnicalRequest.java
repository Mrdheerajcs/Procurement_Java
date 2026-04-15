package com.procurement.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BidTechnicalRequest {
    private Long tenderId;
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
}