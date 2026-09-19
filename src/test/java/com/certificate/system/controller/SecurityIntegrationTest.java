package com.certificate.system.controller;

import com.certificate.system.dto.DashboardDto;
import com.certificate.system.dto.admin.AdminDashboardDto;
import com.certificate.system.entity.User;
import com.certificate.system.service.AdminService;
import com.certificate.system.service.AuditLogService;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogService auditLogService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserPortalService userPortalService;

    @MockBean
    private AdminService adminService;

    // --- Public routes ---
    @Test
    void loginPageIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/login"))
               .andExpect(status().isOk());
    }

    @Test
    void registerPageIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/register"))
               .andExpect(status().isOk());
    }

    @Test
    void healthEndpointIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/actuator/health"))
               .andExpect(status().isOk());
    }

    // --- Unauthenticated access to protected routes ---
    @Test
    void unauthenticated_accessToUserDashboard_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/user/dashboard"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void unauthenticated_accessToAdminDashboard_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrlPattern("**/login"));
    }

    // --- Role-based access control ---
    @Test
    @WithMockUser(username = "user@test.com", authorities = {"ROLE_USER"})
    void roleUser_canAccessUserDashboard() throws Exception {
        User mockUser = new User();
        mockUser.setEmail("user@test.com");
        mockUser.setFirstName("Test");
        mockUser.setLastName("User");

        DashboardDto dto = new DashboardDto();
        dto.setFullName("Test User");

        Mockito.when(userService.findByEmail("user@test.com")).thenReturn(mockUser);
        Mockito.when(userPortalService.buildDashboard(mockUser)).thenReturn(dto);

        mockMvc.perform(get("/user/dashboard"))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@system.com", authorities = {"ROLE_ADMIN"})
    void roleAdmin_canAccessAdminDashboard() throws Exception {
        Mockito.when(adminService.buildDashboard()).thenReturn(new AdminDashboardDto());

        mockMvc.perform(get("/admin/dashboard"))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = {"ROLE_USER"})
    void roleUser_cannotAccessAdminDashboard_getForbidden() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@system.com", authorities = {"ROLE_ADMIN"})
    void roleAdmin_cannotAccessUserArea_isForbidden() throws Exception {
        mockMvc.perform(get("/user/dashboard"))
               .andExpect(status().isForbidden());
    }

    // --- Login with seeded admin (uses real DB via test profile H2 + DatabaseSeeder) ---
    @Test
    void validLogin_withAdminCredentials_redirectsToDashboard() throws Exception {
        mockMvc.perform(formLogin("/login")
                .userParameter("email")
                .user("admin@system.com")
                .password("admin123"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/dashboard-redirect"));
    }

    @Test
    void invalidLogin_wrongPassword_redirectsWithError() throws Exception {
        mockMvc.perform(formLogin("/login")
                .userParameter("email")
                .user("admin@system.com")
                .password("wrongpassword"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/login?error=true"));
    }
}
