package com.procurement.mapper;


import com.procurement.dto.VenderDto;
import com.procurement.dto.request.VenderRegRequest;

import com.procurement.entity.Vendor;
import org.springframework.stereotype.Component;
@Component
public class VenderMapper {
    public Vendor toEntity(VenderRegRequest request) {
        Vendor vendor = new Vendor();
        vendor.setStatus(request.getStatus());
        return vendor;
    }
    public VenderDto toDto(Vendor vendor) {
        VenderDto dto = new VenderDto();
        dto.setStatus(vendor.getStatus());
        return dto;
    }
}
