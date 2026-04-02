package com.procurement.mapper;

import com.procurement.dto.request.VendorTypeRequest;
import com.procurement.dto.responce.VendorTypeDto;
import com.procurement.entity.VendorType;
import org.springframework.stereotype.Component;

@Component
public class VendorTypeMapper {

    public VendorType toEntity(VendorTypeRequest req) {
        VendorType v = new VendorType();
        v.setVendorTypeCode(req.getVendorTypeCode());
        v.setVendorTypeName(req.getVendorTypeName());
        return v;
    }

    public void updateEntity(VendorType v, VendorTypeRequest req) {
        v.setVendorTypeCode(req.getVendorTypeCode());
        v.setVendorTypeName(req.getVendorTypeName());
    }

    public VendorTypeDto toDto(VendorType v) {
        VendorTypeDto dto = new VendorTypeDto();

        dto.setVendorTypeId(v.getVendorTypeId());
        dto.setVendorTypeCode(v.getVendorTypeCode());
        dto.setVendorTypeName(v.getVendorTypeName());
        dto.setStatus(v.getStatus());

        return dto;
    }
}