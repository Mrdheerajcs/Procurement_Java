package com.procurement.service.impl;

import com.procurement.dto.request.TenderTypeRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.TenderTypeDto;
import com.procurement.entity.TenderType;
import com.procurement.mapper.TenderTypeMapper;
import com.procurement.repository.TenderTypeRepository;
import com.procurement.service.TenderTypeService;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenderTypeServiceImpl implements TenderTypeService {

    private final TenderTypeRepository repo;
    private final TenderTypeMapper mapper;

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<TenderTypeDto>> create(TenderTypeRequest req) {
        TenderType t = mapper.toEntity(req);
        repo.save(t);
        return ResponseUtil.success(mapper.toDto(t), "Created");
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<TenderTypeDto>> update(TenderTypeRequest req) {

        TenderType t = repo.findById(req.getTenderTypeId()).orElseThrow();
        mapper.updateEntity(t, req);

        return ResponseUtil.success(mapper.toDto(t), "Updated");
    }

    @Override
    public ResponseEntity<ApiResponse<TenderTypeDto>> getById(Long id) {
        return ResponseUtil.success(mapper.toDto(repo.findById(id).orElseThrow()), "Success");
    }

    @Override
    public ResponseEntity<ApiResponse<List<TenderTypeDto>>> getAll() {
        return ResponseUtil.success(
                repo.findAll().stream().map(mapper::toDto).toList(),
                "Success"
        );
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<String>> changeStatus(Long id, String status) {
        TenderType t = repo.findById(id).orElseThrow();
        t.setStatus(status);
        return ResponseUtil.success("Status Updated", "Success");
    }
}