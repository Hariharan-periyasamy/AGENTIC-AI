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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private AuditLogService auditLogService;

    @MockBean
    private CertificateRepository certificateRepository;

    @Test
    void searchForm_rendersOk() throws Exception {
        mockMvc.perform(get("/verify"))
               .andExpect(status().isOk())
               .andExpect(view().name("public/verify-search"));
    }

    @Test
    void processSearch_validInput_redirects() throws Exception {
        mockMvc.perform(post("/verify/search")
                .param("code", "CERT-123")
                .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/verify/CERT-123"));
    }

    @Test
    void processSearch_emptyInput_redirectsWithFlash() throws Exception {
        mockMvc.perform(post("/verify/search")
                .param("code", "   ")
                .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/verify"))
               .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void verifyCertificate_validCode_returnsValidView() throws Exception {
        User u = new User();
        u.setFirstName("John");
        u.setLastName("Doe");

        CertificateType ct = new CertificateType();
        ct.setName("Degree");

        CertificateRequest req = new CertificateRequest();
        req.setUser(u);
        req.setCertificateType(ct);

        Certificate cert = new Certificate();
        cert.setUuid("test-uuid");
        cert.setCertificateNumber("CERT-001");
        cert.setCertificateRequest(req);
        cert.setIssuedAt(LocalDateTime.now());

        Mockito.when(certificateRepository.findByUuidOrCertificateNumber("CERT-001")).thenReturn(Optional.of(cert));

        mockMvc.perform(get("/verify/CERT-001"))
               .andExpect(status().isOk())
               .andExpect(view().name("public/verify"))
               .andExpect(model().attribute("valid", true))
               .andExpect(model().attributeExists("certificate"));
    }

    @Test
    void verifyCertificate_invalidCode_returnsInvalidView() throws Exception {
        Mockito.when(certificateRepository.findByUuidOrCertificateNumber("bad-code")).thenReturn(Optional.empty());

        mockMvc.perform(get("/verify/bad-code"))
               .andExpect(status().isOk())
               .andExpect(view().name("public/verify"))
               .andExpect(model().attribute("valid", false))
               .andExpect(model().attributeExists("searchedCode"));
    }
}


