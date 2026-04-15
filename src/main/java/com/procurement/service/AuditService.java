package com.procurement.service;

import com.procurement.entity.AuditLog;

public interface AuditService {
    void log(String action, String entityType, Long entityId, String oldValue, String newValue, String remarks);
    void log(String action, String entityType, Long entityId, String oldValue, String newValue);
    void log(String action, String entityType, Long entityId);
    void logLogin(String username, boolean success, String ipAddress);
    void logLogout(String username);
}