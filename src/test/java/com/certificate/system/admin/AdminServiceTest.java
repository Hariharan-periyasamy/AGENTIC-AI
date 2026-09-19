package com.certificate.system.admin;

import com.certificate.system.service.AuditLogService;

import com.certificate.system.entity.CertificateRequest;
import com.certificate.system.entity.CertificateType;
import com.certificate.system.entity.User;
import com.certificate.system.entity.enums.RequestStatus;
import com.certificate.system.exception.InvalidRequestOperationException;
import com.certificate.system.repository.CertificateRequestRepository;
import com.certificate.system.repository.NotificationRepository;
import com.certificate.system.repository.UserRepository;
import com.certificate.system.service.AdminService;
import com.certificate.system.service.CertificateGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private CertificateRequestRepository requestRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private AuditLogService auditLogService;
    @Mock private CertificateGenerationService certificateGenerationService;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(requestRepository, userRepository, notificationRepository, auditLogService, certificateGenerationService);
    }

    private CertificateRequest mockRequest(RequestStatus status) {
        User u = new User();
        u.setEmail("user@test.com");
        u.setFirstName("John");
        u.setLastName("Doe");

        CertificateType ct = new CertificateType();
        ct.setName("Degree");

        CertificateRequest req = new CertificateRequest();
        req.setId(10L);
        req.setUser(u);
        req.setCertificateType(ct);
        req.setStatus(status);
        return req;
    }

    private User mockAdmin() {
        User a = new User();
        a.setEmail("admin@test.com");
        return a;
    }

    @Test
    void markUnderReview_pendingRequest_updatesStatus() {
        CertificateRequest req = mockRequest(RequestStatus.PENDING);
        Mockito.when(requestRepository.findById(10L)).thenReturn(Optional.of(req));

        adminService.markUnderReview(10L, mockAdmin());

        assertEquals(RequestStatus.UNDER_REVIEW, req.getStatus());
        Mockito.verify(requestRepository).save(req);
        Mockito.verify(auditLogService).log(anyString(), anyString(), anyString());
    }

    @Test
    void approveRequest_valid_updatesStatusAndNotifies() {
        CertificateRequest req = mockRequest(RequestStatus.UNDER_REVIEW);
        Mockito.when(requestRepository.findById(10L)).thenReturn(Optional.of(req));

        adminService.approveRequest(10L, mockAdmin());

        assertEquals(RequestStatus.APPROVED, req.getStatus());
        Mockito.verify(requestRepository).save(req);
        Mockito.verify(notificationRepository).save(any());
        Mockito.verify(certificateGenerationService).generateCertificate(req);
        Mockito.verify(auditLogService).log(anyString(), anyString(), anyString());
    }

    @Test
    void approveRequest_alreadyApproved_throwsException() {
        CertificateRequest req = mockRequest(RequestStatus.APPROVED);
        Mockito.when(requestRepository.findById(10L)).thenReturn(Optional.of(req));

        assertThrows(InvalidRequestOperationException.class, () -> adminService.approveRequest(10L, mockAdmin()));
    }
    
    @Test
    void approveRequest_cancelled_throwsException() {
        CertificateRequest req = mockRequest(RequestStatus.CANCELLED);
        Mockito.when(requestRepository.findById(10L)).thenReturn(Optional.of(req));

        assertThrows(InvalidRequestOperationException.class, () -> adminService.approveRequest(10L, mockAdmin()));
    }

    @Test
    void rejectRequest_valid_updatesStatusAndNotifies() {
        CertificateRequest req = mockRequest(RequestStatus.UNDER_REVIEW);
        Mockito.when(requestRepository.findById(10L)).thenReturn(Optional.of(req));

        adminService.rejectRequest(10L, "Missing documents", mockAdmin());

        assertEquals(RequestStatus.REJECTED, req.getStatus());
        assertEquals("Missing documents", req.getRejectionReason());
        Mockito.verify(requestRepository).save(req);
        Mockito.verify(notificationRepository).save(any());
        Mockito.verify(auditLogService).log(anyString(), anyString(), anyString());
    }

    @Test
    void rejectRequest_missingReason_throwsException() {
        assertThrows(InvalidRequestOperationException.class, () -> adminService.rejectRequest(10L, "   ", mockAdmin()));
    }
    
    @Test
    void rejectRequest_alreadyApproved_throwsException() {
        CertificateRequest req = mockRequest(RequestStatus.APPROVED);
        Mockito.when(requestRepository.findById(10L)).thenReturn(Optional.of(req));

        assertThrows(InvalidRequestOperationException.class, () -> adminService.rejectRequest(10L, "Reason", mockAdmin()));
    }
}

