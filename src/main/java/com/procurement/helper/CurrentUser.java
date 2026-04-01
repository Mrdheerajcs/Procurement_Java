package com.procurement.helper;

import com.procurement.entity.User;
import com.procurement.jwt.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUser {

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser();
        }

        return null;
    }

    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    public static User getCurrentUserOrThrow() {
        User user = getCurrentUser();
        if (user == null) {
            throw new RuntimeException("No authenticated user found");
        }
        return user;
    }
}