package com.procurement.mapper;

import com.procurement.dto.request.MprApprovalRequest;
import com.procurement.dto.responce.MprDetailDTO;
import com.procurement.dto.responce.MprDto;
import com.procurement.entity.MprDetail;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MprDetailMapper {
    public void updateApproval(MprDetail entity, MprApprovalRequest dto) {
        entity.setStatus(dto.getStatus());
        entity.setRemarks(dto.getRemarks());
        entity.setApprovalDate(LocalDateTime.now());
    }
}
