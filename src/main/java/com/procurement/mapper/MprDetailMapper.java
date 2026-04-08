package com.procurement.mapper;

import com.procurement.dto.request.MprApprovalList;
import com.procurement.dto.responce.MprDetailDTO;
import com.procurement.entity.BaseEntity;
import com.procurement.entity.MprDetail;
import com.procurement.helper.CurrentUser;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

@Component
public class MprDetailMapper {
    public void updateApproval(MprDetail entity, MprApprovalList req) {
        entity.setStatus(req.getStatus());
        entity.setRemarks(req.getRemarks());
        entity.setApprovalDate(LocalDateTime.now());
        //entity.setUpdateddt(LocalDateTime.now());
        entity.setAuditFields(CurrentUser.getCurrentUserOrThrow().getUsername(),false);
    }

    public MprDetailDTO toDto(MprDetail detail) {
        MprDetailDTO dto = new MprDetailDTO();
        dto.setMprDetailId(detail.getMprDetailId());
       // dto.setLastUpdatedDt(LocalDateTime.now());
        //dto.setUpdatedBy(CurrentUser.getCurrentUserOrThrow().getUsername());
        dto.setStatus(detail.getStatus());
        dto.setRemarks(detail.getRemarks());
        return dto;
    }

    public void mapDtoToEntity(MprDetailDTO dto, MprDetail entity) {
        entity.setItemCode(dto.getItemCode());
        entity.setItemName(dto.getItemName());
        entity.setUom(dto.getUom());
        entity.setSpecification(dto.getSpecificationn());
        entity.setRequestedQty(dto.getRequestedQty());
        entity.setEstimatedRate(dto.getEstimatedRate());
        entity.setEstimatedValue(dto.getEstimatedValue());
        entity.setStockAvailable(dto.getStockAvailable());
        entity.setStatus(dto.getStatus());
        entity.setApprovalDate(dto.getApprovalDate());
        entity.setRemarks(dto.getRemarks());
        entity.setAuditFields(CurrentUser.getCurrentUserOrThrow().getUsername(), true);
    }
}
