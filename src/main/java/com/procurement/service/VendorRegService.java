package com.procurement.service;

import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.VenderDto;
import com.procurement.dto.request.VenderRegRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface VendorRegService {

    ResponseEntity<ApiResponse<VenderDto>> venReg(VenderRegRequest request);

    ResponseEntity<ApiResponse<VenderDto>> updateVendor(Long vendorId, VenderRegRequest request);

    ResponseEntity<ApiResponse<VenderDto>> getVendorById(Long vendorId);

    ResponseEntity<ApiResponse<List<VenderDto>>> getAllVendors();

    ResponseEntity<ApiResponse<String>> deleteVendor(Long vendorId);

    ResponseEntity<ApiResponse<VenderDto>> changeVendorStatus(Long vendorId, String status);
}