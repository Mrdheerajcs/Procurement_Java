package com.procurement.service;

import com.procurement.dto.request.MprTypeRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.MprTypeDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface MprTypeService {
    ResponseEntity<ApiResponse<MprTypeDto>> create(MprTypeRequest req);
    ResponseEntity<ApiResponse<MprTypeDto>> update(MprTypeRequest req);
    ResponseEntity<ApiResponse<MprTypeDto>> getById(Long id);
    ResponseEntity<ApiResponse<List<MprTypeDto>>> getAll();
    ResponseEntity<ApiResponse<String>> changeStatus(Long id, String status);
}