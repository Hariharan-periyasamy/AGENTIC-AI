package com.certificate.system.integration;

import com.certificate.system.entity.Certificate;
import com.certificate.system.entity.CertificateRequest;
import com.certificate.system.entity.Notification;
import com.certificate.system.entity.User;
import com.certificate.system.entity.enums.RequestStatus;
import com.certificate.system.repository.CertificateRepository;
import com.certificate.system.repository.CertificateRequestRepository;
import com.certificate.system.repository.NotificationRepository;
import com.certificate.system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CompleteWorkflowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private CertificateRequestRepository requestRepository;
    @Autowired private CertificateRepository certificateRepository;
    @Autowired private NotificationRepository notificationRepository;

    // Helper to get latest request for a user
    private CertificateRequest getLatestRequest(String email) {
        User u = userRepository.findByEmail(email).orElseThrow();
        List<CertificateRequest> reqs = requestRepository.findByUserOrderByCreatedAtDesc(u);
        return reqs.isEmpty() ? null : reqs.get(0);
    }

    // WORKFLOW 1: Register -> Create -> Admin Review/Approve -> Generate -> Verify
    @Test
    void workflow_1_completeApprovalCycle() throws Exception {
        // 1. Register User
        mockMvc.perform(post("/register")
                .param("firstName", "Alice")
                .param("lastName", "Approval")
                .param("email", "alice_wf1@test.com")
                .param("password", "Pass123")
                .param("confirmPassword", "Pass123")
                .with(csrf()))
               .andExpect(status().is3xxRedirection());

        // 2. Create Request (As Alice)
        MockMultipartFile file = new MockMultipartFile("document", "doc.pdf", "application/pdf", "dummy".getBytes());
        mockMvc.perform(multipart("/user/requests/new")
                .file(file)
                .param("certificateTypeId", "1")
                .param("purpose", "Job Application")
                .param("remarks", "Need it fast")
                .with(csrf())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("alice_wf1@test.com").roles("USER")))
               .andExpect(status().is3xxRedirection());

        CertificateRequest req = getLatestRequest("alice_wf1@test.com");
        assertNotNull(req);
        assertEquals(RequestStatus.PENDING, req.getStatus());

        // 3. Admin Reviews Request
        mockMvc.perform(get("/admin/requests/" + req.getId())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin@system.com").roles("ADMIN")))
               .andExpect(status().isOk());

        req = requestRepository.findById(req.getId()).orElseThrow();
        assertEquals(RequestStatus.UNDER_REVIEW, req.getStatus());

        // 4. Admin Approves Request
        mockMvc.perform(post("/admin/requests/" + req.getId() + "/approve")
                .with(csrf())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin@system.com").roles("ADMIN")))
               .andExpect(status().is3xxRedirection());

        req = requestRepository.findById(req.getId()).orElseThrow();
        assertEquals(RequestStatus.APPROVED, req.getStatus());

        // 5. Verify Certificate was generated
        Certificate cert = certificateRepository.findByCertificateRequest(req).orElseThrow();
        assertNotNull(cert.getUuid());

        // 6. Public Verify Endpoint
        mockMvc.perform(get("/api/certificates/verify/" + cert.getUuid()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.valid").value(true))
               .andExpect(jsonPath("$.holderName").value("Alice Approval"));
    }

    // WORKFLOW 2: Register -> Create -> Admin Reject -> View Reason
    @Test
    void workflow_2_completeRejectionCycle() throws Exception {
        // Register Bob
        mockMvc.perform(post("/register")
                .param("firstName", "Bob")
                .param("lastName", "Reject")
                .param("email", "bob_wf2@test.com")
                .param("password", "Pass123")
                .param("confirmPassword", "Pass123")
                .with(csrf()))
               .andExpect(status().is3xxRedirection());

        // Bob submits a request
        MockMultipartFile file = new MockMultipartFile("document", "doc.pdf", "application/pdf", "dummy".getBytes());
        mockMvc.perform(multipart("/user/requests/new")
                .file(file)
                .param("certificateTypeId", "1")
                .param("purpose", "Transfer")
                .with(csrf())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("bob_wf2@test.com").roles("USER")))
               .andExpect(status().is3xxRedirection());

        CertificateRequest req = getLatestRequest("bob_wf2@test.com");
        assertNotNull(req, "Request should have been created for bob_wf2@test.com");

        // Admin Rejects
        mockMvc.perform(post("/admin/requests/" + req.getId() + "/reject")
                .param("rejectionReason", "Document is blurry")
                .with(csrf())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin@system.com").roles("ADMIN")))
               .andExpect(status().is3xxRedirection());

        req = requestRepository.findById(req.getId()).orElseThrow();
        assertEquals(RequestStatus.REJECTED, req.getStatus());
        assertEquals("Document is blurry", req.getRejectionReason());

        // User views notification
        User bob = userRepository.findByEmail("bob_wf2@test.com").orElseThrow();
        List<Notification> notifs = notificationRepository.findByUserOrderByCreatedAtDesc(bob);
        assertTrue(notifs.stream().anyMatch(n -> n.getMessage().contains("rejected")));
    }

    // WORKFLOW 3: Unauthorized user attempts admin page
    @Test
    @WithMockUser(username = "bob_wf2@test.com", authorities = "ROLE_USER")
    void workflow_3_unauthorizedAdminAccess() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
               .andExpect(status().isForbidden());
    }

    // WORKFLOW 4: User A attempts to cancel User B's request — returns 404 (ResourceNotFoundException)
    @Test
    void workflow_4_crossUserAccessDenied() throws Exception {
        // The service uses findByIdAndUser which throws ResourceNotFoundException (→ 404)
        // when a user tries to access a request that doesn't belong to them.
        mockMvc.perform(post("/user/requests/1/cancel")
                .with(csrf())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("hacker@test.com").roles("USER")))
               .andExpect(status().isNotFound()); // ResourceNotFoundException → GlobalExceptionHandler → 404
    }

    // WORKFLOW 5: Invalid certificate verification
    @Test
    void workflow_5_invalidCertificateVerification() throws Exception {
        mockMvc.perform(get("/api/certificates/verify/FAKE-CERT-CODE"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.valid").value(false));
    }
}

