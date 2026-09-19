package com.certificate.system.controller;

import com.certificate.system.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminAuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogRepository auditLogRepository;

    @Test
    @WithMockUser(username = "admin@test.com", authorities = "ROLE_ADMIN")
    void listAuditLogs_adminAccess_rendersOk() throws Exception {
        Mockito.when(auditLogRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/audit"))
               .andExpect(status().isOk())
               .andExpect(view().name("admin/audit-logs"))
               .andExpect(model().attributeExists("logs"));
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = "ROLE_USER")
    void listAuditLogs_userAccess_forbidden() throws Exception {
        mockMvc.perform(get("/admin/audit"))
               .andExpect(status().isForbidden());
    }
}

