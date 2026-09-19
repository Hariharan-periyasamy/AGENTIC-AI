package com.certificate.system.service;

import com.certificate.system.entity.AuditLog;
import com.certificate.system.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Logs an audit entry in a new transaction so it is always persisted,
     * even if the calling transaction is rolled back.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String performedBy, String details) {
        AuditLog entry = new AuditLog();
        entry.setAction(action);
        entry.setPerformedBy(performedBy);
        entry.setDetails(details);
        auditLogRepository.save(entry);
        logger.info("AUDIT | action={} | by={} | details={}", action, performedBy, details);
    }
}
