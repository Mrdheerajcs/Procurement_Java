package com.procurement.service.impl;

import com.procurement.dto.request.VendorTypeRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.VendorTypeDto;
import com.procurement.entity.VendorType;
import com.procurement.mapper.VendorTypeMapper;
import com.procurement.repository.VendorTypeRepository;
import com.procurement.service.VendorTypeService;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorTypeServiceImpl implements VendorTypeService {

    private final VendorTypeRepository repo;
    private final VendorTypeMapper mapper;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<VendorTypeDto>> create(VendorTypeRequest request) {

        VendorType v = mapper.toEntity(request);
        repo.save(v);

        return ResponseUtil.success(mapper.toDto(v), "Vendor Type created");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<VendorTypeDto>> update(VendorTypeRequest request) {

        VendorType v = repo.findById(request.getVendorTypeId())
                .orElseThrow(() -> new RuntimeException("Vendor Type not found"));

        mapper.updateEntity(v, request);

        return ResponseUtil.success(mapper.toDto(v), "Vendor Type updated");
    }

    @Override
    public ResponseEntity<ApiResponse<VendorTypeDto>> getById(Long id) {

        VendorType v = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor Type not found"));

        return ResponseUtil.success(mapper.toDto(v), "Success");
    }

    @Override
    public ResponseEntity<ApiResponse<List<VendorTypeDto>>> getAll() {

        List<VendorTypeDto> list = repo.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();

        return ResponseUtil.success(list, "Success");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<String>> changeStatus(Long id, String status) {

        VendorType v = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor Type not found"));

        v.setStatus(status);

        return ResponseUtil.success("Status updated", "Success");
    }
}