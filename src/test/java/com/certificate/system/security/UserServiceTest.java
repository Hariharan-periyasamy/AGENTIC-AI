package com.certificate.system.security;

import com.certificate.system.service.AuditLogService;

import com.certificate.system.dto.UserRegistrationDto;
import com.certificate.system.entity.Role;
import com.certificate.system.entity.User;
import com.certificate.system.entity.enums.RoleName;
import com.certificate.system.exception.EmailAlreadyExistsException;
import com.certificate.system.exception.PasswordMismatchException;
import com.certificate.system.repository.RoleRepository;
import com.certificate.system.repository.UserRepository;
import com.certificate.system.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuditLogService auditLogService;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository, passwordEncoder, auditLogService);
    }

    private UserRegistrationDto buildDto(String email, String pass, String confirm) {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFirstName("Test");
        dto.setLastName("User");
        dto.setEmail(email);
        dto.setPassword(pass);
        dto.setConfirmPassword(confirm);
        return dto;
    }

    @Test
    void registerUser_success_passwordIsHashed() {
        Role role = new Role();
        role.setName(RoleName.ROLE_USER);

        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(role));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserRegistrationDto dto = buildDto("test@example.com", "password123", "password123");
        User saved = userService.registerUser(dto);

        assertNotNull(saved);
        assertNotEquals("password123", saved.getPassword());
        assertTrue(passwordEncoder.matches("password123", saved.getPassword()),
                "Password must be BCrypt-encoded");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_passwordMismatch_throwsException() {
        UserRegistrationDto dto = buildDto("test@example.com", "password123", "wrong");
        assertThrows(PasswordMismatchException.class, () -> userService.registerUser(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_duplicateEmail_throwsException() {
        User existing = new User();
        existing.setEmail("dup@example.com");
        when(userRepository.findByEmail("dup@example.com")).thenReturn(Optional.of(existing));

        UserRegistrationDto dto = buildDto("dup@example.com", "pass", "pass");
        assertThrows(EmailAlreadyExistsException.class, () -> userService.registerUser(dto));
        verify(userRepository, never()).save(any());
    }
}


