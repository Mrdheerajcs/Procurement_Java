package com.procurement.dto.responce;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class MprResponse {
    private String mprId;
    private String mprNo;
    private LocalDate mprDate;
    private long departmentId;
    private long mprTypeId;
    private long tenderTypeId;
    private String projectName;
    private String priority;
    private LocalDate requiredByDate;
    private String deliverySchedule;
    private Integer durationDays;
    private String specialNotes;
    private String justification;
    private BigDecimal totalValue;
    private String departmentName;
    private String documentPath;
    private String mprTypeName;
    private String tenderTypeName;
    private List<MprDetailResponnce> mprDetailResponnces;
}