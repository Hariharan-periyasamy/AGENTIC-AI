package com.certificate.system.admin;

import com.certificate.system.service.AuditLogService;

import com.certificate.system.dto.admin.AdminDashboardDto;
import com.certificate.system.entity.CertificateRequest;
import com.certificate.system.entity.CertificateType;
import com.certificate.system.entity.User;
import com.certificate.system.entity.enums.RequestStatus;
import com.certificate.system.exception.InvalidRequestOperationException;
import com.certificate.system.service.AdminService;
import com.certificate.system.service.UserService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private AuditLogService auditLogService;

    @MockBean
    private AdminService adminService;

    @MockBean
    private UserService userService;

    private User mockAdmin() {
        User u = new User();
        u.setEmail("admin@test.com");
        return u;
    }

    private CertificateRequest mockRequest() {
        User u = new User();
        u.setEmail("user@test.com");
        u.setFirstName("Test");
        u.setLastName("User");
        CertificateType ct = new CertificateType();
        ct.setName("Type");
        CertificateRequest req = new CertificateRequest();
        req.setId(1L);
        req.setStatus(RequestStatus.PENDING);
        req.setUser(u);
        req.setCertificateType(ct);
        return req;
    }

    @Test
    @WithMockUser(username = "admin@test.com", authorities = "ROLE_ADMIN")
    void dashboard_rendersOk() throws Exception {
        Mockito.when(adminService.buildDashboard()).thenReturn(new AdminDashboardDto());

        mockMvc.perform(get("/admin/dashboard"))
               .andExpect(status().isOk())
               .andExpect(view().name("admin/dashboard"))
               .andExpect(model().attributeExists("dashboard"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", authorities = "ROLE_ADMIN")
    void listRequests_rendersOk() throws Exception {
        Mockito.when(adminService.searchRequests(any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/requests"))
               .andExpect(status().isOk())
               .andExpect(view().name("admin/requests"))
               .andExpect(model().attributeExists("requests"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", authorities = "ROLE_ADMIN")
    void viewRequest_marksUnderReviewAndRendersOk() throws Exception {
        User admin = mockAdmin();
        Mockito.when(userService.findByEmail("admin@test.com")).thenReturn(admin);
        Mockito.when(adminService.getRequestById(1L)).thenReturn(mockRequest());

        mockMvc.perform(get("/admin/requests/1"))
               .andExpect(status().isOk())
               .andExpect(view().name("admin/request-detail"))
               .andExpect(model().attributeExists("certRequest"));

        Mockito.verify(adminService).markUnderReview(1L, admin);
    }

    @Test
    @WithMockUser(username = "admin@test.com", authorities = "ROLE_ADMIN")
    void approveRequest_valid_redirectsWithSuccess() throws Exception {
        Mockito.when(userService.findByEmail("admin@test.com")).thenReturn(mockAdmin());
        Mockito.doNothing().when(adminService).approveRequest(anyLong(), any());

        mockMvc.perform(post("/admin/requests/1/approve").with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/admin/requests/1"))
               .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", authorities = "ROLE_ADMIN")
    void rejectRequest_valid_redirectsWithSuccess() throws Exception {
        Mockito.when(userService.findByEmail("admin@test.com")).thenReturn(mockAdmin());
        Mockito.doNothing().when(adminService).rejectRequest(anyLong(), anyString(), any());

        mockMvc.perform(post("/admin/requests/1/reject")
                .param("rejectionReason", "Bad docs")
                .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/admin/requests/1"))
               .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", authorities = "ROLE_ADMIN")
    void approveRequest_invalidState_redirectsWithError() throws Exception {
        Mockito.when(userService.findByEmail("admin@test.com")).thenReturn(mockAdmin());
        Mockito.doThrow(new InvalidRequestOperationException("Cannot approve"))
               .when(adminService).approveRequest(anyLong(), any());

        mockMvc.perform(post("/admin/requests/1/approve").with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/admin/requests/1"))
               .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = "ROLE_USER")
    void userAccess_adminPortal_forbidden() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
               .andExpect(status().isForbidden());
    }
}


