package com.procurement.service;

import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.DashboardResponse;
import org.springframework.http.ResponseEntity;

public interface DashboardService {

    ResponseEntity<ApiResponse<DashboardResponse>> getAdminDashboard();

    ResponseEntity<ApiResponse<DashboardResponse>> getVendorDashboard();
}