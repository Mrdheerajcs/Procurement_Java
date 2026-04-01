package com.procurement.mapper;

import com.procurement.dto.request.RegisterRequest;
import com.procurement.dto.responce.UserDTO;
import com.procurement.entity.User;
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