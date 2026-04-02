package com.procurement.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentRequest {
    private Integer departmentId;
    private String departmentName;
    private String departmentCode;
    private String headOfDepartment;
    private String description;
}