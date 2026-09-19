package com.certificate.system.service;

import com.certificate.system.service.AuditLogService;

import com.certificate.system.entity.Certificate;
import com.certificate.system.entity.CertificateRequest;
import com.certificate.system.entity.CertificateType;
import com.certificate.system.entity.User;
import com.certificate.system.entity.enums.RequestStatus;
import com.certificate.system.exception.InvalidRequestOperationException;
import com.certificate.system.repository.CertificateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class CertificateGenerationServiceTest {

    @Mock
    private CertificateRepository certificateRepository;
    @org.mockito.Mock
    private com.certificate.system.service.AuditLogService auditLogService;

    private CertificateGenerationService generationService;

    @BeforeEach
    void setUp() throws IOException {
        generationService = new CertificateGenerationService(certificateRepository, auditLogService);
        Path tempDir = Files.createTempDirectory("certs");
        ReflectionTestUtils.setField(generationService, "certificateDir", tempDir.toString());
        ReflectionTestUtils.setField(generationService, "baseUrl", "http://localhost");
        generationService.init();
    }

    private CertificateRequest mockRequest(RequestStatus status) {
        User u = new User();
        u.setFirstName("John");
        u.setLastName("Doe");

        CertificateType ct = new CertificateType();
        ct.setName("Diploma");

        CertificateRequest req = new CertificateRequest();
        req.setId(99L);
        req.setUser(u);
        req.setCertificateType(ct);
        req.setStatus(status);
        return req;
    }

    @Test
    void generateCertificate_invalidStatus_throwsException() {
        CertificateRequest req = mockRequest(RequestStatus.PENDING);
        assertThrows(InvalidRequestOperationException.class, () -> generationService.generateCertificate(req));
    }

    @Test
    void generateCertificate_alreadyExists_throwsException() {
        CertificateRequest req = mockRequest(RequestStatus.APPROVED);
        Mockito.when(certificateRepository.findByCertificateRequest(req)).thenReturn(Optional.of(new Certificate()));
        
        assertThrows(InvalidRequestOperationException.class, () -> generationService.generateCertificate(req));
    }

    @Test
    void generateCertificate_validApprovedRequest_generatesPdfAndSaves() {
        CertificateRequest req = mockRequest(RequestStatus.APPROVED);
        Mockito.when(certificateRepository.findByCertificateRequest(req)).thenReturn(Optional.empty());
        Mockito.when(certificateRepository.save(any(Certificate.class))).thenAnswer(i -> i.getArgument(0));

        Certificate cert = generationService.generateCertificate(req);

        assertNotNull(cert.getUuid());
        assertNotNull(cert.getCertificateNumber());
        assertNotNull(cert.getPdfUrl());
        assertTrue(cert.getCertificateNumber().contains("CERT-"));
        assertTrue(cert.getPdfUrl().endsWith(".pdf"));
    }
}


