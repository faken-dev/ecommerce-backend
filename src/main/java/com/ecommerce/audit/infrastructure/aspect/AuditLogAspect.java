package com.ecommerce.audit.infrastructure.aspect;

import com.ecommerce.audit.domain.annotation.Audited;
import com.ecommerce.audit.domain.entity.AuditLog;
import com.ecommerce.audit.domain.repository.AuditLogRepository;
import com.ecommerce.shared.infrastructure.security.AuthenticatedUser;
import com.ecommerce.shared.infrastructure.security.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.UUID;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Around("@annotation(audited)")
    public Object audit(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        Object result = null;
        Throwable exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            exception = t;
            throw t;
        } finally {
            logAction(joinPoint, audited, result, exception);
        }
    }

    private void logAction(ProceedingJoinPoint joinPoint, Audited audited, Object result, Throwable exception) {
        try {
            AuthenticatedUser currentUser = SecurityUtils.getCurrentUser();
            UUID userId = currentUser != null ? currentUser.getId() : null;
            String userName = currentUser != null ? currentUser.getFullName() : "Anonymous";
            
            HttpServletRequest request = getCurrentHttpRequest();
            String ipAddress = request != null ? request.getRemoteAddr() : "unknown";
            
            String payload = extractPayload(joinPoint);
            String resourceId = extractResourceId(joinPoint);

            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID())
                    .timestamp(Instant.now())
                    .userId(userId)
                    .userName(userName)
                    .action(audited.action())
                    .resourceType(audited.resource())
                    .resourceId(resourceId)
                    .payload(payload)
                    .ipAddress(ipAddress)
                    .status(exception == null ? "SUCCESS" : "FAILURE")
                    .errorMessage(exception != null ? exception.getMessage() : null)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String extractPayload(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) return null;
            return objectMapper.writeValueAsString(args);
        } catch (Exception e) {
            return "[Error serializing payload]";
        }
    }

    private String extractResourceId(ProceedingJoinPoint joinPoint) {
        // Simple heuristic: if first argument is UUID or String ID, use it
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            Object firstArg = args[0];
            if (firstArg instanceof UUID || firstArg instanceof String) {
                return firstArg.toString();
            }
        }
        return null;
    }
}
