package com.procurement.controller;
import com.procurement.dto.request.ChangePasswordRequest;
import com.procurement.dto.request.LoginRequest;
import com.procurement.dto.request.RegisterRequest;
import com.procurement.dto.responce.ApiResponse;
import com.procurement.dto.responce.AuthResponse;
import com.procurement.dto.responce.UserDTO;
import com.procurement.entity.Role;
import com.procurement.entity.User;
import com.procurement.jwt.JwtUtil;
import com.procurement.mapper.UserMapper;
import com.procurement.repository.RoleRepository;
import com.procurement.repository.UserRepository;
import com.procurement.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.procurement.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseUtil.error(HttpStatus.BAD_REQUEST, "Username already exists!");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseUtil.error(HttpStatus.BAD_REQUEST, "Email already exists!");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role role = roleRepository.findByRoleName("PROCUREMENT_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        user.setRoles(Collections.singleton(role));

        User savedUser = userRepository.save(user);

        return ResponseUtil.success(userMapper.toDto(savedUser), "User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        log.info("Login attempt for user: {}", request.getUsername());

        String clientIp = httpRequest.getRemoteAddr();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            auditService.logLogin(request.getUsername(), true, clientIp);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtUtil.generateToken(authentication);


        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(28800000L) // 8 hours in milliseconds
                .userId(user.getUserId())
                .username(user.getUsername())
                .isPasswordChanged(user.getIsPasswordChanged())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getRoleName).toList())
                .build();

        return ResponseUtil.success(response, "Login successful!");
        } catch (Exception e) {
            auditService.logLogin(request.getUsername(), false, clientIp);
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseUtil.error(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newToken = jwtUtil.generateToken(username);

        AuthResponse response = AuthResponse.builder()
                .token(newToken)
                .tokenType("Bearer")
                .expiresIn(28800000L)
                .userId(user.getUserId())
                .username(user.getUsername())
                .build();

        return ResponseUtil.success(response, "Token refreshed successfully!");
    }
    @PostMapping("/changepassword")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ChangePasswordRequest request) {

        User user = userRepository.findByUsername(request.getUserName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setIsPasswordChanged(true);
        userRepository.save(user);

        return ResponseEntity.ok("Password change successful!");
    }

}