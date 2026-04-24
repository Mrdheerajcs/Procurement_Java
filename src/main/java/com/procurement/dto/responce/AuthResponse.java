package com.procurement.dto.responce;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private Long userId;
    private String username;
    private Boolean isPasswordChanged;
    private String email;
    private String profilePicUrl;  // ✅ ADD THIS
    private List<String> roles;
}