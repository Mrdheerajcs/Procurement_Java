package com.procurement.service;

import com.procurement.dto.request.DepartmentRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.DepartmentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DepartmentService {

    ResponseEntity<ApiResponse<DepartmentDto>> create(DepartmentRequest request);

    ResponseEntity<ApiResponse<DepartmentDto>> update(DepartmentRequest request);

    ResponseEntity<ApiResponse<DepartmentDto>> getById(Integer id);

    ResponseEntity<ApiResponse<List<DepartmentDto>>> getAll();

    ResponseEntity<ApiResponse<String>> changeStatus(Integer id, String status);
}