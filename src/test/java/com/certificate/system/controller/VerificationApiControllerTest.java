package com.certificate.system.controller;

import com.certificate.system.service.AuditLogService;

import com.certificate.system.entity.Certificate;
import com.certificate.system.entity.CertificateRequest;
import com.certificate.system.entity.CertificateType;
import com.certificate.system.entity.User;
import com.certificate.system.repository.CertificateRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VerificationApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private AuditLogService auditLogService;

    @MockBean
    private CertificateRepository certificateRepository;

    @Test
    void verifyApi_validCode_returnsJsonWithValidTrue() throws Exception {
        User u = new User();
        u.setFirstName("Jane");
        u.setLastName("Smith");

        CertificateType ct = new CertificateType();
        ct.setName("Diploma");

        CertificateRequest req = new CertificateRequest();
        req.setUser(u);
        req.setCertificateType(ct);

        Certificate cert = new Certificate();
        cert.setUuid("abc-123");
        cert.setCertificateNumber("CERT-999");
        cert.setCertificateRequest(req);
        cert.setIssuedAt(LocalDateTime.now());

        Mockito.when(certificateRepository.findByUuidOrCertificateNumber("CERT-999")).thenReturn(Optional.of(cert));

        mockMvc.perform(get("/api/certificates/verify/CERT-999"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.valid").value(true))
               .andExpect(jsonPath("$.certificateNumber").value("CERT-999"))
               .andExpect(jsonPath("$.holderName").value("Jane Smith"))
               .andExpect(jsonPath("$.certificateType").value("Diploma"))
               .andExpect(jsonPath("$.issuingAuthority").value("Digital Certificate Authority"))
               .andExpect(jsonPath("$.verificationCode").value("abc-123"));
    }

    @Test
    void verifyApi_invalidCode_returnsJsonWithValidFalse() throws Exception {
        Mockito.when(certificateRepository.findByUuidOrCertificateNumber("BAD")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/certificates/verify/BAD"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.valid").value(false))
               .andExpect(jsonPath("$.certificateNumber").doesNotExist());
    }

    @Test
    void verifyApi_emptyCode_returnsBadRequest() throws Exception {
        // Since Spring MVC paths might strip trailing whitespace, testing just a space can be tricky with path variables.
        // We can test if the method handles it if it gets through.
        mockMvc.perform(get("/api/certificates/verify/ "))
               .andExpect(status().isBadRequest());
    }
}


