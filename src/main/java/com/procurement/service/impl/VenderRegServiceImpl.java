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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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

    @Value("${app.upload.base-dir:C:/uploads}")
    private String baseDir;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<VenderDto>> venReg(VenderRegRequest request) {
        log.info("Starting vendor registration...");

        if (userRepository.findByEmail(request.getEmailId()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

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

        String vendorCode = generateVendorCodeSafe();
        log.info("Generated Vendor Code: {}", vendorCode);

        String rawPassword = request.getMobileNo();
        String passwordHint = generatePasswordHint(rawPassword);

        Vendor vendor = venderMapper.toEntity(request);
        vendor.setVendorCode(vendorCode);

        Role role = roleRepository.findById(3L)
                .orElseThrow(() -> new RuntimeException("ROLE_VENDOR not found"));

        User user = new User();
        user.setUsername(request.getEmailId());
        user.setEmail(request.getEmailId());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setIsPasswordChanged(false);
        user.setIsActive(true);
        user.setIsAccountNonLocked(true);
        user.setRoles(Set.of(role));

        userRepository.save(user);

        vendor.setUserId(user.getUserId());
        vendor.setAuditFields("self", true);
        vendorRepository.save(vendor);

        try {
            sendVendorEmail(user.getEmail(), user.getUsername(), vendorCode, passwordHint);
        } catch (Exception e) {
            log.error("Email sending failed: {}", e.getMessage());
        }

        log.info("Vendor registration completed");

        return ResponseUtil.success(venderMapper.toDto(vendor), "Vendor registered successfully");
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<VenderDto>> updateVendor(Long vendorId, VenderRegRequest request, MultipartFile profilePic) {
        log.info("Updating vendor ID: {}", vendorId);

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        vendor.setVendorName(request.getVendorName());
        vendor.setContactPerson(request.getContactPerson());
        vendor.setMobileNo(request.getMobileNo());
        vendor.setAlternateMobile(request.getAlternateMobile());
        vendor.setAddressLine1(request.getAddressLine1());
        vendor.setAddressLine2(request.getAddressLine2());
        vendor.setCity(request.getCity());
        vendor.setState(request.getState());
        vendor.setCountry(request.getCountry());
        vendor.setPincode(request.getPincode());
        vendor.setGstNo(request.getGstNo());
        vendor.setBankName(request.getBankName());
        vendor.setAccountNo(request.getAccountNo());
        vendor.setIfscCode(request.getIfscCode());
        vendor.setPaymentTermsId(request.getPaymentTermsId());
        vendor.setIsPreferred(request.getIsPreferred());

        // ✅ Update profile picture using UploadConfig baseDir
        if (profilePic != null && !profilePic.isEmpty()) {
            String picPath = saveProfilePic(profilePic, vendor.getUserId());

            // Update in User entity
            User user = userRepository.findById(vendor.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setProfilePicPath(picPath);
            userRepository.save(user);
        }

        vendor.setAuditFields(CurrentUser.getCurrentUserOrThrow().getUsername(), false);
        vendorRepository.save(vendor);

        return ResponseUtil.success(venderMapper.toDto(vendor), "Vendor updated successfully");
    }

    // ✅ FIXED: Save profile pic using UploadConfig baseDir
    private String saveProfilePic(MultipartFile file, Long userId) {
        try {
            String uploadDir = baseDir + "/profiles/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            String fileName = "user_" + userId + "_" + System.currentTimeMillis() + extension;
            File dest = new File(dir, fileName);
            file.transferTo(dest);

            String absolutePath = dest.getAbsolutePath();
            log.info("Profile picture saved at: {}", absolutePath);
            return absolutePath;
        } catch (IOException e) {
            log.error("Error saving profile pic: {}", e.getMessage());
            return null;
        }
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

        return ResponseUtil.success(venderMapper.toDto(vendor), "Vendor status changed to " + vendor.getStatus());
    }

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
                + "Password: " + passwordHint + "\n\n"
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