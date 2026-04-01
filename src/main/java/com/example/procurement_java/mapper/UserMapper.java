package com.example.procurement_java.mapper;

import com.example.procurement_java.dto.RegisterRequest;
import com.example.procurement_java.dto.UserDTO;
import com.example.procurement_java.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        return user;
    }

    public UserDTO toDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setActive(user.getIsActive());
        dto.setAccountNonLocked(user.getIsAccountNonLocked());
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                    .map(role -> role.getRoleName())
                    .toList());
        }
        return dto;
    }
}