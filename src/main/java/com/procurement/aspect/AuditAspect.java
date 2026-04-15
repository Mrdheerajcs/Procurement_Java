package com.procurement.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.procurement.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    // Store old values before update
    private final ThreadLocal<Map<String, Object>> oldValuesHolder = new ThreadLocal<>();

    @Before("@annotation(com.procurement.annotation.Auditable)")
    public void captureOldValues(JoinPoint joinPoint) {
        Object entity = getEntityFromArgs(joinPoint);
        if (entity != null) {
            Map<String, Object> oldValues = new HashMap<>();
            oldValues.put("entity", entity);
            oldValues.put("id", getIdValue(entity));
            oldValues.put("className", entity.getClass().getSimpleName());
            oldValuesHolder.set(oldValues);
        }
    }

    @AfterReturning(pointcut = "@annotation(com.procurement.annotation.Auditable)", returning = "result")
    public void logAfterUpdate(JoinPoint joinPoint, Object result) {
        try {
            Map<String, Object> oldValues = oldValuesHolder.get();
            if (oldValues != null) {
                Object newEntity = getEntityFromArgs(joinPoint);
                if (newEntity != null) {
                    String oldJson = objectMapper.writeValueAsString(oldValues.get("entity"));
                    String newJson = objectMapper.writeValueAsString(newEntity);
                    auditService.log(
                            "UPDATE",
                            (String) oldValues.get("className"),
                            (Long) oldValues.get("id"),
                            oldJson,
                            newJson,
                            "Entity updated via " + joinPoint.getSignature().getName()
                    );
                }
                oldValuesHolder.remove();
            }
        } catch (Exception e) {
            log.error("Audit aspect error: {}", e.getMessage());
        }
    }

    private Object getEntityFromArgs(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg != null && !arg.getClass().getName().startsWith("org.springframework")) {
                return arg;
            }
        }
        return null;
    }

    private Long getIdValue(Object entity) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return (Long) idField.get(entity);
        } catch (Exception e) {
            return null;
        }
    }
}