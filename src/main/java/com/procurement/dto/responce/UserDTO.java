package com.procurement.dto.responce;

import lombok.Data;
import java.util.Set;

@Data
public class UserDTO {
    private Long userId;
    private String username;
    private String email;
    private Boolean isActive;
    private Boolean isPasswordChanged;
    private String profilePicUrl;  // ✅ ADD THIS
    private Set<String> roles;
}