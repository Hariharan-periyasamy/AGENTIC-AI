package com.certificate.system.service;

import com.certificate.system.dto.CertificateRequestSubmitDto;
import com.certificate.system.entity.CertificateRequest;
import com.certificate.system.entity.CertificateType;
import com.certificate.system.entity.User;
import com.certificate.system.entity.enums.RequestStatus;
import com.certificate.system.exception.DuplicateRequestException;
import com.certificate.system.exception.InvalidRequestOperationException;
import com.certificate.system.repository.CertificateRepository;
import com.certificate.system.repository.CertificateRequestRepository;
import com.certificate.system.repository.CertificateTypeRepository;
import com.certificate.system.repository.NotificationRepository;
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
class UserPortalServiceTest {

    @Mock private CertificateRequestRepository requestRepository;
    @Mock private CertificateRepository certificateRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private CertificateTypeRepository typeRepository;
    @Mock private AuditLogService auditLogService;

    private UserPortalService userPortalService;

    @BeforeEach
    void setUp() {
        // Correct constructor: (CertificateRequestRepository, CertificateRepository,
        //                       NotificationRepository, CertificateTypeRepository, AuditLogService)
        userPortalService = new UserPortalService(requestRepository, certificateRepository, notificationRepository, typeRepository, auditLogService);
    }

    private User mockUser() {
        User u = new User();
        u.setEmail("user@test.com");
        return u;
    }

    private CertificateType mockType() {
        CertificateType t = new CertificateType();
        t.setId(1L);
        t.setName("Degree");
        return t;
    }

    @Test
    void submitRequest_validData_savesRequestAndNotifies() {
        User user = mockUser();
        CertificateType type = mockType();

        CertificateRequestSubmitDto dto = new CertificateRequestSubmitDto();
        dto.setCertificateTypeId(1L);
        dto.setPurpose("Job");

        // submitRequest(User, CertificateRequestSubmitDto, String storedFilename)
        Mockito.when(typeRepository.findById(1L)).thenReturn(Optional.of(type));
        Mockito.when(requestRepository.countActiveDuplicates(user, type)).thenReturn(0L);

        CertificateRequest savedReq = new CertificateRequest();
        savedReq.setId(10L);
        Mockito.when(requestRepository.save(any(CertificateRequest.class))).thenReturn(savedReq);

        userPortalService.submitRequest(user, dto, "doc.pdf");

        Mockito.verify(requestRepository).save(any(CertificateRequest.class));
        Mockito.verify(notificationRepository).save(any());
        Mockito.verify(auditLogService).log(anyString(), anyString(), anyString());
    }

    @Test
    void submitRequest_duplicateActive_throwsException() {
        User user = mockUser();
        CertificateType type = mockType();

        CertificateRequestSubmitDto dto = new CertificateRequestSubmitDto();
        dto.setCertificateTypeId(1L);

        Mockito.when(typeRepository.findById(1L)).thenReturn(Optional.of(type));
        Mockito.when(requestRepository.countActiveDuplicates(user, type)).thenReturn(1L);

        assertThrows(DuplicateRequestException.class, () -> userPortalService.submitRequest(user, dto, "doc.pdf"));
    }

    @Test
    void cancelRequest_pendingStatus_cancelsSuccessfully() {
        User user = mockUser();
        CertificateRequest req = new CertificateRequest();
        req.setStatus(RequestStatus.PENDING);
        req.setUser(user);

        Mockito.when(requestRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(req));

        userPortalService.cancelRequest(10L, user);

        assertEquals(RequestStatus.CANCELLED, req.getStatus());
        Mockito.verify(requestRepository).save(req);
        Mockito.verify(auditLogService).log(anyString(), anyString(), anyString());
    }

    @Test
    void cancelRequest_underReviewStatus_throwsException() {
        User user = mockUser();
        CertificateRequest req = new CertificateRequest();
        req.setStatus(RequestStatus.UNDER_REVIEW);
        req.setUser(user);

        Mockito.when(requestRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(req));

        assertThrows(InvalidRequestOperationException.class, () -> userPortalService.cancelRequest(10L, user));
    }
}
