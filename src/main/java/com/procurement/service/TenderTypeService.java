package com.procurement.service;

import com.procurement.dto.request.TenderTypeRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.TenderTypeDto;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TenderTypeService {
    @Transactional
    ResponseEntity<ApiResponse<TenderTypeDto>> create(TenderTypeRequest req);

    @Transactional
    ResponseEntity<ApiResponse<TenderTypeDto>> update(TenderTypeRequest req);

    ResponseEntity<ApiResponse<TenderTypeDto>> getById(Long id);

    ResponseEntity<ApiResponse<List<TenderTypeDto>>> getAll();

    @Transactional
    ResponseEntity<ApiResponse<String>> changeStatus(Long id, String status);
}
