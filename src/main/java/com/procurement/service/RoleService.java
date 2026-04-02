package com.procurement.service;

import com.procurement.dto.request.RoleRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.RoleDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface RoleService {

    ResponseEntity<ApiResponse<RoleDto>> create(RoleRequest request);

    ResponseEntity<ApiResponse<RoleDto>> update(RoleRequest request);

    ResponseEntity<ApiResponse<RoleDto>> getById(Long id);

    ResponseEntity<ApiResponse<List<RoleDto>>> getAll();

    ResponseEntity<ApiResponse<String>> changeStatus(Long id, String status);
}