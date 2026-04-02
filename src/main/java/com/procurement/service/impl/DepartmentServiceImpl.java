package com.procurement.service.impl;

import com.procurement.dto.request.DepartmentRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.DepartmentDto;
import com.procurement.entity.Department;
import com.procurement.helper.CurrentUser;
import com.procurement.mapper.DepartmentMapper;
import com.procurement.repository.DepartmentRepository;
import com.procurement.service.DepartmentService;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository repo;
    private final DepartmentMapper mapper;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<DepartmentDto>> create(DepartmentRequest request) {

        Department d = mapper.toEntity(request);

        d.setAuditFields(CurrentUser.getCurrentUserOrThrow().getUsername(), true);

        repo.save(d);

        return ResponseUtil.success(mapper.toDto(d), "Created");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<DepartmentDto>> update(DepartmentRequest request) {

        Department d = repo.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Not found"));

        mapper.updateEntity(d, request);

        d.setAuditFields(CurrentUser.getCurrentUserOrThrow().getUsername(), false);

        return ResponseUtil.success(mapper.toDto(d), "Updated");
    }

    @Override
    public ResponseEntity<ApiResponse<DepartmentDto>> getById(Integer id) {

        Department d = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        return ResponseUtil.success(mapper.toDto(d), "Success");
    }

    @Override
    public ResponseEntity<ApiResponse<List<DepartmentDto>>> getAll() {

        List<DepartmentDto> list = repo.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();

        return ResponseUtil.success(list, "Success");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<String>> changeStatus(Integer id, String status) {

        Department d = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        d.setIsActive(status);

        return ResponseUtil.success("Status Updated", "Success");
    }
}