package com.procurement.mapper;

import com.procurement.dto.request.DepartmentRequest;
import com.procurement.dto.responce.DepartmentDto;
import com.procurement.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public Department toEntity(DepartmentRequest req) {
        Department d = new Department();

        d.setDepartmentName(req.getDepartmentName());
        d.setDepartmentCode(req.getDepartmentCode());
        d.setHeadOfDepartment(req.getHeadOfDepartment());
        d.setDescription(req.getDescription());

        return d;
    }

    public void updateEntity(Department d, DepartmentRequest req) {
        d.setDepartmentName(req.getDepartmentName());
        d.setDepartmentCode(req.getDepartmentCode());
        d.setHeadOfDepartment(req.getHeadOfDepartment());
        d.setDescription(req.getDescription());
    }

    public DepartmentDto toDto(Department d) {
        DepartmentDto dto = new DepartmentDto();

        dto.setDepartmentId(d.getDepartmentId());
        dto.setDepartmentName(d.getDepartmentName());
        dto.setDepartmentCode(d.getDepartmentCode());
        dto.setHeadOfDepartment(d.getHeadOfDepartment());
        dto.setDescription(d.getDescription());
        dto.setIsActive(d.getIsActive());

        dto.setCreatedBy(d.getCreatedBy());
        dto.setUpdatedBy(d.getUpdatedBy());
        dto.setLastUpdatedDt(d.getLastUpdatedDt());

        return dto;
    }
}