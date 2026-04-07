package com.procurement.dto.request;

import com.procurement.dto.responce.MprDetailDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
@Getter
@Setter
public class MprUpdateRequest {
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
    //private String status;
    private List<MprDetailDTO> details;
    private List<Long> deleteDetailIds; // delete ke liye
}
