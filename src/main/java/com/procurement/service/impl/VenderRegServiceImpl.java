package com.procurement.service.impl;

import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.VenderDto;
import com.procurement.dto.request.VenderRegRequest;
import com.procurement.entity.Role;
import com.procurement.entity.User;
import com.procurement.entity.Vendor;
import com.procurement.helper.CurrentUser;
import com.procurement.mapper.VenderMapper;
import com.procurement.repository.RoleRepository;
import com.procurement.repository.UserRepository;
import com.procurement.repository.VendorRepository;
import com.procurement.service.VendorRegService;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VenderRegServiceImpl implements VendorRegService {

    private final VendorRepository vendorRepository;
    private final VenderMapper venderMapper;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<VenderDto>> venReg(VenderRegRequest request) {

        Vendor vendor = venderMapper.toEntity(request);

        Role role = roleRepository.findById(3L)
                .orElseThrow(() -> new RuntimeException("ROLE_VENDER not found"));

        User user = new User();
        user.setUsername(request.getEmailId());
        user.setEmail(request.getEmailId());
        user.setPassword(passwordEncoder.encode(request.getMobileNo()));
        user.setIsPasswordChanged(false);
        user.setIsActive(true);
        user.setIsAccountNonLocked(true);
        user.setRoles(Set.of(role));

        userRepository.save(user);

        vendor.setUserId(user.getUserId());

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

        User user = userRepository.findById(vendor.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found for vendor"));

        user.setUsername(request.getEmailId());
        user.setEmail(request.getEmailId());

        userRepository.save(user);

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

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        if (vendor.getUserId() != null) {
            userRepository.deleteById(vendor.getUserId());
        }

        vendorRepository.deleteById(vendorId);

        return ResponseUtil.success("Vendor deleted successfully");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<VenderDto>> changeVendorStatus(Long vendorId, String status) {

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        if (!"Y".equalsIgnoreCase(status) && !"N".equalsIgnoreCase(status)) {
            throw new RuntimeException("Status must be 'Y' or 'N'");
        }

        vendor.setStatus(status.toUpperCase());
        vendorRepository.save(vendor);

        User user = userRepository.findById(vendor.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isActive = "Y".equalsIgnoreCase(status);

        user.setIsActive(isActive);
        user.setIsAccountNonLocked(isActive);

        userRepository.save(user);

        return ResponseUtil.success(
                venderMapper.toDto(vendor),
                "Vendor status changed to " + vendor.getStatus()
        );
    }
}