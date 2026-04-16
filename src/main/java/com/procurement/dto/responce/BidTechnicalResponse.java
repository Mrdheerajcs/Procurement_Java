package com.procurement.dto.responce;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BidTechnicalResponse {
    private Long bidTechnicalId;
    private Long tenderId;
    private String tenderNo;
    private String tenderTitle;
    private Long vendorId;
    private String vendorName;
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
    private String evaluationStatus;
    private Integer evaluationScore;
    private String evaluationRemarks;
    private LocalDateTime submittedAt;

    private String submissionStatus;
    private String clarificationQuestion;
    private LocalDateTime clarificationDeadline;
    private String vendorResponse;            // Vendor's response
}