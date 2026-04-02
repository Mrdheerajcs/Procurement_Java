package com.procurement.mapper;

import com.procurement.dto.request.TenderTypeRequest;
import com.procurement.dto.responce.TenderTypeDto;
import com.procurement.entity.TenderType;
import org.springframework.stereotype.Component;

@Component
public class TenderTypeMapper {

    public TenderType toEntity(TenderTypeRequest req) {
        TenderType t = new TenderType();
        t.setTenderCode(req.getTenderCode());
        t.setTenderName(req.getTenderName());
        return t;
    }

    public void updateEntity(TenderType t, TenderTypeRequest req) {
        t.setTenderCode(req.getTenderCode());
        t.setTenderName(req.getTenderName());
    }

    public TenderTypeDto toDto(TenderType t) {
        TenderTypeDto dto = new TenderTypeDto();

        dto.setTenderTypeId(t.getTenderTypeId());
        dto.setTenderCode(t.getTenderCode());
        dto.setTenderName(t.getTenderName());
        dto.setStatus(t.getStatus());

        return dto;
    }
}