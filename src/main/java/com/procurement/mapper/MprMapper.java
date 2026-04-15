package com.procurement.mapper;

import com.procurement.dto.request.MprRequest;
import com.procurement.dto.responce.MprDto;
import com.procurement.entity.MprHeader;
import com.procurement.entity.Priority;
import com.procurement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MprMapper {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TenderTypeRepository tenderTypeRepository;

    @Autowired
    private MprTypeRepository mprTypeRepository;

    public MprHeader toEntity(MprRequest request) {
        if (request == null) return null;

        MprHeader mprHeader = new MprHeader();
        mprHeader.setMprNo(request.getMprNo());
        mprHeader.setMprDate(request.getMprDate());
        mprHeader.setProjectName(request.getProjectName());
        mprHeader.setPriority(Priority.valueOf(request.getPriority()));
        mprHeader.setRequiredByDate(request.getRequiredByDate());
        mprHeader.setDeliverySchedule(request.getDeliverySchedule());
        mprHeader.setDurationDays(request.getDurationDays());
        mprHeader.setSpecialNotes(request.getSpecialNotes());
        mprHeader.setJustification(request.getJustification());
        mprHeader.setStatus("Y");
        mprHeader.setCreatedAt(java.time.LocalDateTime.now());

        // ✅ FIX: Convert Integer to Long safely
        Integer deptId = request.getDepartmentId() != null ? request.getDepartmentId() : null;
        if (deptId != null) {
            mprHeader.setDepartment(
                    departmentRepository.findById(deptId)
                            .orElseThrow(() -> new RuntimeException("Department not found for ID: " + deptId))
            );
        }

        if (request.getMprTypeId() != null) {
            mprHeader.setMprType(
                    mprTypeRepository.findById(Long.valueOf(request.getMprTypeId()))
                            .orElseThrow(() -> new RuntimeException("MPR Type not found for ID: " + request.getMprTypeId()))
            );
        }

        if (request.getTenderTypeId() != null) {
            mprHeader.setTenderType(
                    tenderTypeRepository.findById(Long.valueOf(request.getTenderTypeId()))
                            .orElseThrow(() -> new RuntimeException("Tender Type not found for ID: " + request.getTenderTypeId()))
            );
        }

        return mprHeader;
    }

    public MprDto toDto(MprHeader mprHeader) {
        if (mprHeader == null) return null;

        MprDto dto = new MprDto();
        dto.setMprId(mprHeader.getMprId());
        dto.setMprNo(mprHeader.getMprNo());
        dto.setMprDate(mprHeader.getMprDate());

        dto.setDepartmentId(
                mprHeader.getDepartment() != null ? mprHeader.getDepartment().getDepartmentId() : null
        );

        dto.setMprTypeId(
                mprHeader.getMprType() != null ? mprHeader.getMprType().getTypeId() : null
        );

        dto.setTenderTypeId(
                mprHeader.getTenderType() != null ? mprHeader.getTenderType().getTenderTypeId() : null
        );

        dto.setProjectName(mprHeader.getProjectName());
        dto.setPriority(String.valueOf(mprHeader.getPriority()));
        dto.setRequiredByDate(mprHeader.getRequiredByDate());
        dto.setDeliverySchedule(mprHeader.getDeliverySchedule());
        dto.setDurationDays(mprHeader.getDurationDays());
        dto.setSpecialNotes(mprHeader.getSpecialNotes());
        dto.setJustification(mprHeader.getJustification());
        dto.setStatus(mprHeader.getStatus());
        dto.setLastUpdatedDt(mprHeader.getLastUpdatedDt());

        return dto;
    }
}