package com.procurement.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class MprRequest {
    private String mprNo;
    private LocalDate mprDate;
    private Integer departmentId;
    private Integer mprTypeId;
    private Integer tenderTypeId;
    private String projectName;
    private String priority;
    private LocalDate requiredByDate;
    private String deliverySchedule;
    private Integer durationDays;
    private String specialNotes;
    private String justification;
    private BigDecimal totalValue;
    private String documentPath;           // ✅ NEW - ADD THIS LINE
    private List<MprDetailRequest> mprDetailRequests;
}