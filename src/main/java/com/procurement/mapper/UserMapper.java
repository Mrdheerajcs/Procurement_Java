package com.procurement.mapper;

import com.procurement.dto.request.RegisterRequest;
import com.procurement.dto.responce.UserDTO;
import com.procurement.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    @Value("${app.upload.base-dir:C:/uploads}")
    private String baseDir;

    public UserDTO toDto(User user) {
        if (user == null) return null;

        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setIsActive(user.getIsActive());
        dto.setIsPasswordChanged(user.getIsPasswordChanged());

        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                    .map(role -> role.getRoleName())
                    .collect(Collectors.toSet()));
        }

        // ✅ Generate URL for profile picture using UploadConfig baseDir
        if (user.getProfilePicPath() != null && !user.getProfilePicPath().isEmpty()) {
            File file = new File(user.getProfilePicPath());
            if (file.exists()) {
                String fileName = file.getName();
                dto.setProfilePicUrl("/api/users/profile-pic/" + fileName);
            }
        }

        return dto;
    }

    public User toEntity(RegisterRequest request) {
        if (request == null) return null;

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        return user;
    }
}