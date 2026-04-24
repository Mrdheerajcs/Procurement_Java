package com.procurement.dto.responce;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MprDto {
    private Long mprId;
    private String mprNo;
    private LocalDate mprDate;
    // Foreign Keys (Only IDs in DTO)
    private Long departmentId;
    private Long mprTypeId;
    private Long tenderTypeId;

    private String projectName;
    private String priority;
    private LocalDate requiredByDate;
    private String deliverySchedule;
    private Integer durationDays;
    private String specialNotes;
    private String justification;
    private String status;


    private BigDecimal totalValue;

    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime lastUpdatedDt;

}
