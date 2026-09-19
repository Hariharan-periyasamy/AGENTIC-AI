package com.certificate.system.security;

import com.certificate.system.dto.DashboardDto;
import com.certificate.system.entity.CertificateRequest;
import com.certificate.system.entity.CertificateType;
import com.certificate.system.entity.User;
import com.certificate.system.entity.enums.RequestStatus;
import com.certificate.system.exception.ResourceNotFoundException;
import com.certificate.system.repository.CertificateRepository;
import com.certificate.system.repository.CertificateRequestRepository;
import com.certificate.system.repository.CertificateTypeRepository;
import com.certificate.system.repository.NotificationRepository;
import com.certificate.system.service.AuditLogService;
import com.certificate.system.service.UserPortalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPortalServiceTest {

    @Mock private CertificateRequestRepository requestRepository;
    @Mock private CertificateRepository certificateRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private CertificateTypeRepository typeRepository;
    @Mock private AuditLogService auditLogService;

    private UserPortalService service;

    @BeforeEach
    void setUp() {
        service = new UserPortalService(requestRepository, certificateRepository, notificationRepository, typeRepository, auditLogService);
    }

    private User buildUser(Long id, String email) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setFirstName("Test");
        u.setLastName("User");
        return u;
    }

    @Test
    void buildDashboard_returnsCorrectCounts() {
        User user = buildUser(1L, "u@test.com");
        when(requestRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(Collections.emptyList());
        when(requestRepository.countByUserAndStatus(user, RequestStatus.PENDING)).thenReturn(2L);
        when(requestRepository.countByUserAndStatus(user, RequestStatus.APPROVED)).thenReturn(1L);
        when(requestRepository.countByUserAndStatus(user, RequestStatus.REJECTED)).thenReturn(0L);
        when(notificationRepository.countByUserAndReadFalse(user)).thenReturn(3L);

        DashboardDto dto = service.buildDashboard(user);
        assertEquals(2L, dto.getPendingRequests());
        assertEquals(1L, dto.getApprovedRequests());
        assertEquals(3L, dto.getUnreadNotifications());
    }

    @Test
    void getRequestForUser_notOwner_throwsResourceNotFoundException() {
        User user = buildUser(1L, "u@test.com");
        when(requestRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getRequestForUser(99L, user));
    }

    @Test
    void getRequestForUser_owner_returnsDto() {
        User user = buildUser(1L, "u@test.com");

        CertificateType ct = new CertificateType();
        ct.setName("Degree Certificate");

        CertificateRequest req = new CertificateRequest();
        req.setId(10L);
        req.setUser(user);
        req.setCertificateType(ct);
        req.setStatus(RequestStatus.PENDING);

        when(requestRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(req));
        when(certificateRepository.findByCertificateRequest(req)).thenReturn(Optional.empty());

        var dto = service.getRequestForUser(10L, user);
        assertEquals(10L, dto.getId());
        assertEquals("Degree Certificate", dto.getCertificateTypeName());
        assertFalse(dto.isHasCertificate());
    }
}
