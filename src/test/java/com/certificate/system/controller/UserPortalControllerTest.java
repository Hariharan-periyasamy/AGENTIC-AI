package com.certificate.system.controller;

import com.certificate.system.service.AuditLogService;

import com.certificate.system.dto.DashboardDto;
import com.certificate.system.dto.CertificateRequestDto;
import com.certificate.system.dto.NotificationDto;
import com.certificate.system.dto.UserProfileDto;
import com.certificate.system.entity.User;
import com.certificate.system.service.UserPortalService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserPortalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private AuditLogService auditLogService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserPortalService userPortalService;

    private User mockUser() {
        User u = new User();
        u.setId(1L);
        u.setEmail("user@test.com");
        u.setFirstName("Test");
        u.setLastName("User");
        return u;
    }

    // ---- Dashboard ----
    @Test
    @WithMockUser(username = "user@test.com", authorities = "ROLE_USER")
    void dashboard_rendersOk() throws Exception {
        User user = mockUser();
        DashboardDto dto = new DashboardDto();
        dto.setFullName("Test User");
        dto.setTotalRequests(2);

        Mockito.when(userService.findByEmail("user@test.com")).thenReturn(user);
        Mockito.when(userPortalService.buildDashboard(user)).thenReturn(dto);

        mockMvc.perform(get("/user/dashboard"))
               .andExpect(status().isOk())
               .andExpect(view().name("user/dashboard"))
               .andExpect(model().attributeExists("dashboard"));
    }

    // ---- Profile ----
    @Test
    @WithMockUser(username = "user@test.com", authorities = "ROLE_USER")
    void profile_get_rendersOk() throws Exception {
        UserProfileDto dto = new UserProfileDto();
        dto.setFirstName("Test");
        dto.setLastName("User");
        dto.setEmail("user@test.com");

        Mockito.when(userService.getProfileDto("user@test.com")).thenReturn(dto);

        mockMvc.perform(get("/user/profile"))
               .andExpect(status().isOk())
               .andExpect(view().name("user/profile"))
               .andExpect(model().attributeExists("profileDto"));
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = "ROLE_USER")
    void profile_post_validData_redirects() throws Exception {
        Mockito.doNothing().when(userService).updateProfile(anyString(), any());

        mockMvc.perform(post("/user/profile")
                .with(csrf())
                .param("firstName", "Updated")
                .param("lastName", "Name"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/user/profile"));
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = "ROLE_USER")
    void profile_post_blankFirstName_returnsForm() throws Exception {
        mockMvc.perform(post("/user/profile")
                .with(csrf())
                .param("firstName", "")
                .param("lastName", "Name"))
               .andExpect(status().isOk())
               .andExpect(view().name("user/profile"));
    }

    // ---- Requests ----
    @Test
    @WithMockUser(username = "user@test.com", authorities = "ROLE_USER")
    void requests_list_rendersOk() throws Exception {
        User user = mockUser();
        Mockito.when(userService.findByEmail("user@test.com")).thenReturn(user);
        Mockito.when(userPortalService.getRequestsForUser(user)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/user/requests"))
               .andExpect(status().isOk())
               .andExpect(view().name("user/requests"))
               .andExpect(model().attributeExists("requests"));
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = "ROLE_USER")
    void request_detail_rendersOk() throws Exception {
        User user = mockUser();
        CertificateRequestDto dto = new CertificateRequestDto();
        dto.setId(1L);
        dto.setCertificateTypeName("Degree Certificate");
        dto.setStatus(com.certificate.system.entity.enums.RequestStatus.PENDING);

        Mockito.when(userService.findByEmail("user@test.com")).thenReturn(user);
        Mockito.when(userPortalService.getRequestForUser(1L, user)).thenReturn(dto);

        mockMvc.perform(get("/user/requests/1"))
               .andExpect(status().isOk())
               .andExpect(view().name("user/request-detail"))
               .andExpect(model().attributeExists("certRequest"));
    }

    // ---- Data isolation: user cannot access another user's request ----
    @Test
    @WithMockUser(username = "user@test.com", authorities = "ROLE_USER")
    void request_detail_wrongOwner_notFound() throws Exception {
        User user = mockUser();
        Mockito.when(userService.findByEmail("user@test.com")).thenReturn(user);
        Mockito.when(userPortalService.getRequestForUser(99L, user))
               .thenThrow(new com.certificate.system.exception.ResourceNotFoundException("Access denied"));

        mockMvc.perform(get("/user/requests/99"))
               .andExpect(status().isNotFound());
    }

    // ---- Certificates ----
    @Test
    @WithMockUser(username = "user@test.com", authorities = "ROLE_USER")
    void certificates_rendersOk() throws Exception {
        User user = mockUser();
        Mockito.when(userService.findByEmail("user@test.com")).thenReturn(user);
        Mockito.when(userPortalService.getRequestsForUser(user)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/user/certificates"))
               .andExpect(status().isOk())
               .andExpect(view().name("user/certificates"))
               .andExpect(model().attributeExists("certificates"));
    }

    // ---- Notifications ----
    @Test
    @WithMockUser(username = "user@test.com", authorities = "ROLE_USER")
    void notifications_rendersOk() throws Exception {
        User user = mockUser();
        Mockito.when(userService.findByEmail("user@test.com")).thenReturn(user);
        Mockito.when(userPortalService.getNotificationsForUser(user)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/user/notifications"))
               .andExpect(status().isOk())
               .andExpect(view().name("user/notifications"))
               .andExpect(model().attributeExists("notifications"));
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = "ROLE_USER")
    void notifications_markAllRead_redirects() throws Exception {
        User user = mockUser();
        Mockito.when(userService.findByEmail("user@test.com")).thenReturn(user);
        Mockito.doNothing().when(userPortalService).markAllNotificationsRead(user);

        mockMvc.perform(post("/user/notifications/mark-all-read")
                .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/user/notifications"));
    }

    // ---- Authorization: ROLE_ADMIN cannot access /user/** ----
    @Test
    @WithMockUser(username = "admin@system.com", authorities = "ROLE_ADMIN")
    void admin_cannotAccess_userPortal() throws Exception {
        mockMvc.perform(get("/user/dashboard"))
               .andExpect(status().isForbidden());
    }

    // ---- Unauthenticated redirects to login ----
    @Test
    void unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/user/dashboard"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrlPattern("**/login"));
    }
}


