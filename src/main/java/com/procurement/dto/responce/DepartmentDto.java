package com.procurement.dto.responce;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DepartmentDto {
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private String headOfDepartment;
    private String description;
    private String isActive;

    private String createdBy;
    private String updatedBy;
    private LocalDateTime lastUpdatedDt;
}