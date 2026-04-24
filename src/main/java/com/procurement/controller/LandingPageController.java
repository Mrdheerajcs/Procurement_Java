package com.procurement.controller;

import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.LandingPageResponse;
import com.procurement.service.LandingPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/landing")
@RequiredArgsConstructor
public class LandingPageController {

    private final LandingPageService landingPageService;

    @GetMapping("/data")
    public ResponseEntity<ApiResponse<LandingPageResponse>> getLandingPageData() {
        log.info("API: Get landing page data (public)");
        return landingPageService.getLandingPageData();
    }
}