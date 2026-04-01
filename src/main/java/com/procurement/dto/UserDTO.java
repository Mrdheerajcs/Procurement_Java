package com.procurement.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserDTO {
    private Long userId;
    private String username;
    private String email;
    private Boolean active;
    private Boolean accountNonLocked;
    private List<String> roles;
}