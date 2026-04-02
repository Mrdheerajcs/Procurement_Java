package com.procurement.service.impl;

import com.procurement.dto.request.MprTypeRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.MprTypeDto;
import com.procurement.entity.MprType;
import com.procurement.helper.CurrentUser;
import com.procurement.mapper.MprTypeMapper;
import com.procurement.repository.MprTypeRepository;
import com.procurement.service.MprTypeService;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MprTypeServiceImpl implements MprTypeService {

    private final MprTypeRepository repo;
    private final MprTypeMapper mapper;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<MprTypeDto>> create(MprTypeRequest req) {

        MprType m = mapper.toEntity(req);
        m.setAuditFields(CurrentUser.getCurrentUserOrThrow().getUsername(), true);

        repo.save(m);

        return ResponseUtil.success(mapper.toDto(m), "Created");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<MprTypeDto>> update(MprTypeRequest req) {

        MprType m = repo.findById(req.getTypeId())
                .orElseThrow(() -> new RuntimeException("Not found"));

        mapper.updateEntity(m, req);
        m.setAuditFields(CurrentUser.getCurrentUserOrThrow().getUsername(), false);

        return ResponseUtil.success(mapper.toDto(m), "Updated");
    }

    @Override
    public ResponseEntity<ApiResponse<MprTypeDto>> getById(Long id) {
        return ResponseUtil.success(
                mapper.toDto(repo.findById(id).orElseThrow()),
                "Success"
        );
    }

    @Override
    public ResponseEntity<ApiResponse<List<MprTypeDto>>> getAll() {
        return ResponseUtil.success(
                repo.findAll().stream().map(mapper::toDto).toList(),
                "Success"
        );
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<String>> changeStatus(Long id, String status) {

        MprType m = repo.findById(id).orElseThrow();
        m.setStatus(status);

        return ResponseUtil.success("Status Updated", "Success");
    }
}