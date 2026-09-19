package com.certificate.system.service;

import com.certificate.system.dto.UserProfileDto;
import com.certificate.system.dto.UserRegistrationDto;
import com.certificate.system.entity.Role;
import com.certificate.system.entity.User;
import com.certificate.system.entity.enums.RoleName;
import com.certificate.system.exception.EmailAlreadyExistsException;
import com.certificate.system.exception.PasswordMismatchException;
import com.certificate.system.exception.ResourceNotFoundException;
import com.certificate.system.repository.RoleRepository;
import com.certificate.system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public User registerUser(UserRegistrationDto dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match.");
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already registered: " + dto.getEmail());
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("ROLE_USER not found in database."));

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        User saved = userRepository.save(user);
        logger.info("New user registered: email={}", saved.getEmail());
        
        // Audit Registration
        auditLogService.log("USER_REGISTRATION", saved.getEmail(), "User registered successfully");
        return saved;
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    @Transactional(readOnly = true)
    public UserProfileDto getProfileDto(String email) {
        User user = findByEmail(email);
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        return dto;
    }

    @Transactional
    public void updateProfile(String email, UserProfileDto dto) {
        User user = findByEmail(email);
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        userRepository.save(user);
        logger.info("Profile updated for user: email={}", email);
        
        auditLogService.log("PROFILE_UPDATE", email, "User updated profile information");
    }
}
