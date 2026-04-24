package com.procurement.service;

import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.LandingPageResponse;
import org.springframework.http.ResponseEntity;

public interface LandingPageService {
    ResponseEntity<ApiResponse<LandingPageResponse>> getLandingPageData();
}