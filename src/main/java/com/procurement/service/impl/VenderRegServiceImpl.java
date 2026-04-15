package com.procurement.service.impl;

import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.VenderDto;
import com.procurement.dto.request.VenderRegRequest;
import com.procurement.entity.Role;
import com.procurement.entity.User;
import com.procurement.entity.Vendor;
import com.procurement.entity.VendorPayment;
import com.procurement.helper.CurrentUser;
import com.procurement.config.VendorFeeConfig;
import com.procurement.mapper.VenderMapper;
import com.procurement.repository.RoleRepository;
import com.procurement.repository.UserRepository;
import com.procurement.repository.VendorPaymentRepository;
import com.procurement.repository.VendorRepository;
import com.procurement.service.VendorRegService;
import com.procurement.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VenderRegServiceImpl implements VendorRegService {

    private final VendorRepository vendorRepository;
    private final VenderMapper venderMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final VendorFeeConfig feeConfig;
    private final JavaMailSender mailSender;

    private final VendorPaymentRepository paymentRepository;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<VenderDto>> venReg(VenderRegRequest request) {

        log.info("Starting vendor registration...");

        // 1. Duplicate email check
        if (userRepository.findByEmail(request.getEmailId()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // 2. Fee check
        if (feeConfig.isEnabled()) {

            VendorPayment payment = paymentRepository
                    .findTopByEmailOrderByPaymentIdDesc(request.getEmailId())
                    .orElseThrow(() -> new RuntimeException("Please complete payment before registration"));

            if (!"SUCCESS".equalsIgnoreCase(payment.getStatus())) {
                throw new RuntimeException("Payment not completed");
            }

            if (!payment.getAmount().equals(feeConfig.getAmount())) {
                throw new RuntimeException("Invalid payment amount");
            }

            log.info("Payment verified for: {}", request.getEmailId());
        }

        // 3. Generate vendor code
        String vendorCode = generateVendorCodeSafe();
        log.info("Generated Vendor Code: {}", vendorCode);

        // 4. Generate password (using mobile)
        String rawPassword = request.getMobileNo();
        String passwordHint = generatePasswordHint(rawPassword);

        // 5. Map entity
        Vendor vendor = venderMapper.toEntity(request);

        // 🔥 IMPORTANT: force set AFTER mapping
        vendor.setVendorCode(vendorCode);

        // 6. Role
        Role role = roleRepository.findById(3L)
                .orElseThrow(() -> new RuntimeException("ROLE_VENDOR not found"));

        // 7. Create user
        User user = new User();
        user.setUsername(request.getEmailId());
        user.setEmail(request.getEmailId());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setIsPasswordChanged(false);
        user.setIsActive(true);
        user.setIsAccountNonLocked(true);
        user.setRoles(Set.of(role));

        userRepository.save(user);

        // 8. Save vendor
        vendor.setUserId(user.getUserId());
        vendor.setAuditFields("self", true);

        vendorRepository.save(vendor);

        // 9. Send email (SAFE)
        try {
            sendVendorEmail(
                    user.getEmail(),
                    user.getUsername(),
                    vendorCode,
                    passwordHint
            );
        } catch (Exception e) {
            log.error("Email sending failed: {}", e.getMessage());
        }

        log.info("Vendor registration completed");

        return ResponseUtil.success(
                venderMapper.toDto(vendor),
                "Vendor registered successfully"
        );
    }

    // ================= UPDATE =================

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<VenderDto>> updateVendor(Long vendorId, VenderRegRequest request) {

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        // ❌ DO NOT update vendorCode

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
        vendor.setDrugLicenseNo(request.getRegistrationNo());
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
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(request.getEmailId());
        user.setEmail(request.getEmailId());

        userRepository.save(user);

        return ResponseUtil.success(
                venderMapper.toDto(vendor),
                "Vendor updated successfully"
        );
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


    // ================= HELPER METHODS =================

    private String generateVendorCode() {
        Long maxId = vendorRepository.findMaxVendorId();
        long nextId = (maxId == null) ? 1 : maxId + 1;
        return String.format("VND%05d", nextId);
    }

    private String generateVendorCodeSafe() {
        for (int i = 0; i < 5; i++) {
            String code = generateVendorCode();
            if (!vendorRepository.existsByVendorCode(code)) {
                return code;
            }
        }
        throw new RuntimeException("Failed to generate unique vendor code");
    }

    private String generatePasswordHint(String password) {
        return password.charAt(0) + "****" + password.charAt(password.length() - 1);
    }

    private void sendVendorEmail(String to, String username, String vendorCode, String passwordHint) {

        String subject = "Vendor Registration Successful";

        String body = "Dear Vendor,\n\n"
                + "Your registration is successful.\n\n"
                + "Username: " + username + "\n"
                + "Vendor Code: " + vendorCode + "\n"
                + "Password Hint: Assume your Mobile No. is 9999988888 than password is same \n"
                + "Password Hint: " + passwordHint + "\n\n"
                + "Please reset your password on first login.\n\n"
                + "Regards,\nProcurement Team";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Email sending failed: {}", e.getMessage());
        }
    }
}