package com.procurement.service.impl;

import com.procurement.dto.ApiResponse;
import com.procurement.dto.VenderDto;
import com.procurement.dto.request.VenderRegRequest;
import com.procurement.entity.Vendor;
import com.procurement.helper.CurrentUser;
import com.procurement.mapper.VenderMapper;
import com.procurement.repository.VendorRepository;
import com.procurement.service.VendorRegService;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VenderRegServiceImpl implements VendorRegService {

    private final VendorRepository vendorRepository;
    private final VenderMapper venderMapper;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<VenderDto>> venReg(VenderRegRequest request) {
        Vendor vendor = venderMapper.toEntity(request);
        vendor.setAuditFields(CurrentUser.getCurrentUserOrThrow().getUsername(), true);
        vendorRepository.save(vendor);
        return ResponseUtil.success(venderMapper.toDto(vendor), "Vendor registered successfully");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<VenderDto>> updateVendor(Long vendorId, VenderRegRequest request) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        vendor.setVendorCode(request.getVendorCode());
        vendor.setVendorName(request.getVendorName());
        vendor.setVendorTypeId(request.getVendorTypeId());
        vendor.setContactPerson(request.getContactPerson());
        vendor.setMobileNo(request.getMobileNo());
        vendor.setAlternateMobile(request.getAlternateMobile());
        vendor.setEmailId(request.getEmailId());
        vendor.setAddressLine1(request.getAddressLine1());
        vendor.setAddressLine2(request.getAddressLine2());
        vendor.setCity(request.getCity());
        vendor.setState(request.getState());
        vendor.setCountry(request.getCountry());
        vendor.setPincode(request.getPincode());
        vendor.setGstNo(request.getGstNo());
        vendor.setPanNo(request.getPanNo());
        vendor.setDrugLicenseNo(request.getDrugLicenseNo());
        vendor.setLicenseValidTill(request.getLicenseValidTill());
        vendor.setBankName(request.getBankName());
        vendor.setAccountNo(request.getAccountNo());
        vendor.setIfscCode(request.getIfscCode());
        vendor.setPaymentTermsId(request.getPaymentTermsId());
        vendor.setIsPreferred(request.getIsPreferred());
        vendor.setIsBlacklisted(request.getIsBlacklisted());
        vendor.setBlacklistReason(request.getBlacklistReason());
        vendor.setAuditFields(CurrentUser.getCurrentUserOrThrow().getUsername(), false);

        vendorRepository.save(vendor);
        return ResponseUtil.success(venderMapper.toDto(vendor), "Vendor updated successfully");
    }

    @Override
    public ResponseEntity<ApiResponse<VenderDto>> getVendorById(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        return ResponseUtil.success(venderMapper.toDto(vendor));
    }

    @Override
    public ResponseEntity<ApiResponse<List<VenderDto>>> getAllVendors() {
        List<VenderDto> vendors = vendorRepository.findAll()
                .stream()
                .map(venderMapper::toDto)
                .collect(Collectors.toList());
        return ResponseUtil.success(vendors);
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<String>> deleteVendor(Long vendorId) {
        vendorRepository.deleteById(vendorId);
        return ResponseUtil.success("Vendor deleted successfully");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<VenderDto>> changeVendorStatus(Long vendorId, String status) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        if (!"Y".equalsIgnoreCase(status) && !"N".equalsIgnoreCase(status)) {
            throw new RuntimeException("Status must be 'Y' (active) or 'N' (inactive)");
        }

        vendor.setStatus(status.toUpperCase());
        vendorRepository.save(vendor);

        return ResponseUtil.success(venderMapper.toDto(vendor),
                "Vendor status changed to " + vendor.getStatus());
    }
}