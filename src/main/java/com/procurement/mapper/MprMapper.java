package com.procurement.mapper;

import com.procurement.dto.request.MprRequest;
import com.procurement.dto.responce.MprDto;
import com.procurement.entity.MprHeader;
import com.procurement.repository.DepartmentRepository;
import com.procurement.repository.MprTypeRepository;
import com.procurement.repository.TenderTypeRepository;
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
        mprHeader.setPriority(request.getPriority());
        mprHeader.setRequiredByDate(request.getRequiredByDate());
        mprHeader.setDeliverySchedule(request.getDeliverySchedule());
        mprHeader.setDurationDays(request.getDurationDays());
        mprHeader.setSpecialNotes(request.getSpecialNotes());
        mprHeader.setJustification(request.getJustification());
        mprHeader.setStatus("Y");

        mprHeader.setDepartment(
                departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new RuntimeException("Department not found"))
        );

        mprHeader.setMprType(
                mprTypeRepository.findById(Long.valueOf(request.getMprTypeId()))
                        .orElseThrow(() -> new RuntimeException("MPR Type not found"))
        );

        mprHeader.setTenderType(
                tenderTypeRepository.findById(Long.valueOf(request.getTenderTypeId()))
                        .orElseThrow(() -> new RuntimeException("Tender Type not found"))
        );

        return mprHeader;
    }

    public MprDto toDto(MprHeader mprHeader) {
        if (mprHeader == null) return null;

        MprDto dto = new MprDto();

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
        dto.setPriority(mprHeader.getPriority());
        dto.setRequiredByDate(mprHeader.getRequiredByDate());
        dto.setDeliverySchedule(mprHeader.getDeliverySchedule());
        dto.setDurationDays(mprHeader.getDurationDays());
        dto.setSpecialNotes(mprHeader.getSpecialNotes());
        dto.setJustification(mprHeader.getJustification());
        dto.setStatus(mprHeader.getStatus());

//        dto.setCreatedBy(mprHeader.getCreatedBy());
//        dto.setUpdatedBy(mprHeader.getUpdatedBy());
        dto.setLastUpdatedDt(mprHeader.getLastUpdatedDt());

        return dto;
    }
}