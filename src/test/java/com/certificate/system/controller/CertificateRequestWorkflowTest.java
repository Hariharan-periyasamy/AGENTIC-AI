package com.certificate.system.controller;

import com.certificate.system.service.AuditLogService;

import com.certificate.system.dto.CertificateRequestSubmitDto;
import com.certificate.system.entity.CertificateRequest;
import com.certificate.system.entity.User;
import com.certificate.system.exception.DuplicateRequestException;
import com.certificate.system.exception.FileStorageException;
import com.certificate.system.exception.InvalidRequestOperationException;
import com.certificate.system.service.FileStorageService;
import com.certificate.system.service.UserPortalService;
import com.certificate.system.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CertificateRequestWorkflowTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private AuditLogService auditLogService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserPortalService userPortalService;

    @MockBean
    private FileStorageService fileStorageService;

    private User mockUser() {
        User u = new User();
        u.setId(1L);
        u.setEmail("user@test.com");
        return u;
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = "ROLE_USER")
    void getNewRequestForm_rendersOk() throws Exception {
        mockMvc.perform(get("/user/requests/new"))
               .andExpect(status().isOk())
               .andExpect(view().name("user/new-request"))
               .andExpect(model().attributeExists("submitDto"))
               .andExpect(model().attributeExists("certificateTypes"));
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = "ROLE_USER")
    void submitRequest_validData_redirectsToRequests() throws Exception {
        User user = mockUser();
        Mockito.when(userService.findByEmail("user@test.com")).thenReturn(user);
        
        MockMultipartFile file = new MockMultipartFile("document", "test.pdf", "application/pdf", "test data".getBytes());
        Mockito.when(fileStorageService.storeFile(any())).thenReturn("stored-uuid.pdf");
        Mockito.when(userPortalService.submitRequest(any(), any(), anyString())).thenReturn(new CertificateRequest());

        mockMvc.perform(multipart("/user/requests/new")
                .file(file)
                .param("certificateTypeId", "1")
                .param("purpose", "Need it for university")
                .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/user/requests"))
               .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = "ROLE_USER")
    void submitRequest_missingDocument_returnsForm() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("document", "", "application/pdf", new byte[0]);
        
        mockMvc.perform(multipart("/user/requests/new")
                .file(emptyFile)
                .param("certificateTypeId", "1")
                .param("purpose", "Test")
                .with(csrf()))
               .andExpect(status().isOk())
               .andExpect(view().name("user/new-request"))
               .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = "ROLE_USER")
    void submitRequest_invalidFile_throwsFileStorageException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("document", "test.exe", "application/octet-stream", "bad".getBytes());
        Mockito.when(fileStorageService.storeFile(any())).thenThrow(new FileStorageException("Invalid file type"));

        mockMvc.perform(multipart("/user/requests/new")
                .file(file)
                .param("certificateTypeId", "1")
                .param("purpose", "Test")
                .with(csrf()))
               .andExpect(status().isOk())
               .andExpect(view().name("user/new-request"))
               .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = "ROLE_USER")
    void submitRequest_duplicateRequest_returnsFormWithError() throws Exception {
        User user = mockUser();
        Mockito.when(userService.findByEmail("user@test.com")).thenReturn(user);
        
        MockMultipartFile file = new MockMultipartFile("document", "test.pdf", "application/pdf", "data".getBytes());
        Mockito.when(fileStorageService.storeFile(any())).thenReturn("stored.pdf");
        Mockito.when(userPortalService.submitRequest(any(), any(), anyString()))
               .thenThrow(new DuplicateRequestException("Duplicate found"));

        mockMvc.perform(multipart("/user/requests/new")
                .file(file)
                .param("certificateTypeId", "1")
                .param("purpose", "Test")
                .with(csrf()))
               .andExpect(status().isOk())
               .andExpect(view().name("user/new-request"))
               .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = "ROLE_USER")
    void cancelRequest_validPendingRequest_redirectsWithSuccess() throws Exception {
        User user = mockUser();
        Mockito.when(userService.findByEmail("user@test.com")).thenReturn(user);
        Mockito.doNothing().when(userPortalService).cancelRequest(1L, user);

        mockMvc.perform(post("/user/requests/1/cancel").with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/user/requests/1"))
               .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = "ROLE_USER")
    void cancelRequest_invalidState_redirectsWithError() throws Exception {
        User user = mockUser();
        Mockito.when(userService.findByEmail("user@test.com")).thenReturn(user);
        Mockito.doThrow(new InvalidRequestOperationException("Cannot cancel"))
               .when(userPortalService).cancelRequest(1L, user);

        mockMvc.perform(post("/user/requests/1/cancel").with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/user/requests/1"))
               .andExpect(flash().attributeExists("errorMessage"));
    }
}


