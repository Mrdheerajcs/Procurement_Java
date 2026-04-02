package com.procurement.service;

import com.procurement.dto.request.VendorTypeRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.VendorTypeDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface VendorTypeService {

    ResponseEntity<ApiResponse<VendorTypeDto>> create(VendorTypeRequest request);

    ResponseEntity<ApiResponse<VendorTypeDto>> update(VendorTypeRequest request);

    ResponseEntity<ApiResponse<VendorTypeDto>> getById(Long id);

    ResponseEntity<ApiResponse<List<VendorTypeDto>>> getAll();

    ResponseEntity<ApiResponse<String>> changeStatus(Long id, String status);
}
