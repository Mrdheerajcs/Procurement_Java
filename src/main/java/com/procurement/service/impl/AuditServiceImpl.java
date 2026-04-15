package com.procurement.service.impl;

import com.procurement.entity.AuditLog;
import com.procurement.helper.CurrentUser;
import com.procurement.repository.AuditLogRepository;
import com.procurement.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return "UNKNOWN";
            HttpServletRequest request = attributes.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty()) {
                ip = request.getRemoteAddr();
            }
            return ip != null ? ip : "UNKNOWN";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return "UNKNOWN";
            return attributes.getRequest().getHeader("User-Agent");
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private String getCurrentUsername() {
        try {
            return CurrentUser.getCurrentUserOrThrow().getUsername();
        } catch (Exception e) {
            return "SYSTEM";
        }
    }

    @Override
    public void log(String action, String entityType, Long entityId, String oldValue, String newValue, String remarks) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .username(getCurrentUsername())
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValue(truncate(oldValue, 2000))
                    .newValue(truncate(newValue, 2000))
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIp())
                    .userAgent(getUserAgent())
                    .remarks(remarks)
                    .build();
            auditLogRepository.save(auditLog);
            log.debug("Audit log saved: {} - {} - {}", action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }

    @Override
    public void log(String action, String entityType, Long entityId, String oldValue, String newValue) {
        log(action, entityType, entityId, oldValue, newValue, null);
    }

    @Override
    public void log(String action, String entityType, Long entityId) {
        log(action, entityType, entityId, null, null, null);
    }

    @Override
    public void logLogin(String username, boolean success, String ipAddress) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .username(username)
                    .action(success ? "LOGIN_SUCCESS" : "LOGIN_FAILED")
                    .entityType("User")
                    .entityId(null)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .userAgent(getUserAgent())
                    .remarks(success ? "Login successful" : "Login failed - invalid credentials")
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save login audit: {}", e.getMessage());
        }
    }

    @Override
    public void logLogout(String username) {
        log("LOGOUT", "User", null, null, null, "User logged out");
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}