package com.procurement.controller;

import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.DashboardResponse;
import com.procurement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<DashboardResponse>> getAdminDashboard() {
        log.info("API: Get admin dashboard data");
        return dashboardService.getAdminDashboard();
    }

    @GetMapping("/vendor")
    public ResponseEntity<ApiResponse<DashboardResponse>> getVendorDashboard() {
        log.info("API: Get vendor dashboard data");
        return dashboardService.getVendorDashboard();
    }
}