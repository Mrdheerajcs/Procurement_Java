package com.procurement.controller;

import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.VenderDto;
import com.procurement.dto.request.VenderRegRequest;
import com.procurement.entity.Vendor;
import com.procurement.helper.CurrentUser;
import com.procurement.mapper.VenderMapper;
import com.procurement.repository.VendorRepository;
import com.procurement.service.VendorRegService;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorRegService vendorRegService;

    private final VenderMapper venderMapper;
    private final VendorRepository vendorRepository;

    @PostMapping("/registration")
    public ResponseEntity<ApiResponse<VenderDto>> venderReg(@RequestBody VenderRegRequest request) {
        log.info("Registering vendor...");
        return vendorRegService.venReg(request);
    }

    @PutMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<VenderDto>> updateVendor(@PathVariable Long vendorId, @RequestBody VenderRegRequest request) {
        log.info("Updating vendor ID: {}", vendorId);
        return vendorRegService.updateVendor(vendorId, request);
    }

    @GetMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<VenderDto>> getVendorById(@PathVariable Long vendorId) {
        log.info("Fetching vendor ID: {}", vendorId);
        return vendorRegService.getVendorById(vendorId);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VenderDto>>> getAllVendors() {
        log.info("Fetching all vendors...");
        return vendorRegService.getAllVendors();
    }

    @DeleteMapping("/{vendorId}")
    public ResponseEntity<ApiResponse<String>> deleteVendor(@PathVariable Long vendorId) {
        log.info("Deleting vendor ID: {}", vendorId);
        return vendorRegService.deleteVendor(vendorId);
    }

    @PatchMapping("/{vendorId}/status")
    public ResponseEntity<ApiResponse<VenderDto>> changeVendorStatus(@PathVariable Long vendorId, @RequestParam String status) {
        log.info("Changing status of vendor ID: {} to {}", vendorId, status);
        return vendorRegService.changeVendorStatus(vendorId, status);
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<VenderDto>> getVendorProfile() {
        log.info("Fetching vendor profile for logged-in user");
        String username = CurrentUser.getCurrentUserOrThrow().getUsername();
        Vendor vendor = vendorRepository.findByEmailId(username)
                .orElseThrow(() -> new RuntimeException("Vendor not found for user: " + username));
        return ResponseUtil.success(venderMapper.toDto(vendor), "Vendor profile retrieved");
    }

}