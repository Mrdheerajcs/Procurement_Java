package com.procurement.mapper;

import com.procurement.dto.request.MprRequest;
import com.procurement.dto.responce.MprDto;
import com.procurement.entity.MprHeader;
import com.procurement.repository.DepartmentRepository;
import com.procurement.repository.MprTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public class MprMapper {
    @Autowired
    DepartmentRepository departmentRepository;
    @Autowired
    MprTypeRepository mprTypeRepository;
    public MprHeader toEntity(MprRequest request) {
        if(request == null) return null;
        MprHeader mprHeader= new MprHeader();
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

        /////
        mprHeader.setDepartment(departmentRepository.findById(request.getDepartmentId()).get());
        mprHeader.setMprType(mprTypeRepository.findById(request.getMprTypeId()).get());
        mprHeader.setTenderType(request.getTenderTypeId());

        return mprHeader;
    }
    public MprDto toDto(MprHeader mprHeader) {
        if (mprHeader == null) return null;
        MprDto mprDto= new MprDto();
        mprDto.setMprNo(mprHeader.getMprNo());
        mprDto.setMprDate(mprHeader.getMprDate());

        mprDto.setDepartmentId(departmentRepository.findById(mprHeader.getDepartmentId()).get());
        mprDto.setMprTypeId(mprHeader.getMprType());
        mprDto.setTenderTypeId(mprHeader.getTenderType());
       // paymentDetail.setBillingHd(billingHeaderRepository.findById(request.getBillHeaderId()).get());


        mprDto.setProjectName(mprHeader.getProjectName());
        mprDto.setPriority(mprHeader.getPriority());
        mprDto.setRequiredByDate(mprHeader.getRequiredByDate());
        mprDto.setDeliverySchedule(mprHeader.getDeliverySchedule());
        mprDto.setDurationDays(mprHeader.getDurationDays());
        mprDto.setSpecialNotes(mprHeader.getSpecialNotes());
        mprDto.setJustification(mprHeader.getJustification());
        mprDto.setStatus(mprHeader.getStatus());
        mprDto.setUpdatedBy(mprHeader.getUpdatedBy());
        mprDto.setCreatedBy(mprHeader.getCreatedBy());
        mprDto.setLastUpdatedDt(mprHeader.getLastUpdatedDt());
        return mprDto;
    }
}
