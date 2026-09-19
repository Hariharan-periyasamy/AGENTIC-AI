package com.certificate.system.service;

import com.certificate.system.service.AuditLogService;

import com.certificate.system.entity.AuditLog;
import com.certificate.system.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void log_savesAuditLogEntry() {
        auditLogService.log("TEST_ACTION", "user@test.com", "Testing details");
        Mockito.verify(auditLogRepository, Mockito.times(1)).save(any(AuditLog.class));
    }
}

