package com.procurement.mapper;

import com.procurement.dto.request.MprTypeRequest;
import com.procurement.dto.responce.MprTypeDto;
import com.procurement.entity.MprType;
import org.springframework.stereotype.Component;

@Component
public class MprTypeMapper {

    public MprType toEntity(MprTypeRequest req) {
        MprType m = new MprType();
        m.setTypeCode(req.getTypeCode());
        m.setTypeName(req.getTypeName());
        return m;
    }

    public void updateEntity(MprType m, MprTypeRequest req) {
        m.setTypeCode(req.getTypeCode());
        m.setTypeName(req.getTypeName());
    }

    public MprTypeDto toDto(MprType m) {
        MprTypeDto dto = new MprTypeDto();

        dto.setTypeId(m.getTypeId());
        dto.setTypeCode(m.getTypeCode());
        dto.setTypeName(m.getTypeName());
        dto.setStatus(m.getStatus());

        dto.setCreatedBy(m.getCreatedBy());
        dto.setUpdatedBy(m.getUpdatedBy());
        dto.setLastUpdatedDt(m.getLastUpdatedDt());

        return dto;
    }
}