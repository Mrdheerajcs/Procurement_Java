package com.procurement.dto.responce;

import com.procurement.dto.request.MprDetailRequest;
import lombok.Data;

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
    private List<MprDetailResponnce> mprDetailResponnces;
}

