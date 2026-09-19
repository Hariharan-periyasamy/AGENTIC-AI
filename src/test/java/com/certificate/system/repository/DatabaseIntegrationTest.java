package com.certificate.system.repository;

import com.certificate.system.entity.Role;
import com.certificate.system.entity.User;
import com.certificate.system.entity.enums.RoleName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CertificateTypeRepository certificateTypeRepository;

    @Test
    void applicationStartsAndDatabaseConnectionWorks() {
        assertNotNull(userRepository);
        assertNotNull(roleRepository);
    }

    @Test
    @Transactional
    void seedDataExistsAndRelationshipsWork() {
        // Verify Roles Seed
        Optional<Role> adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN);
        assertTrue(adminRole.isPresent());

        // Verify Admin User Seed and relationships
        Optional<User> adminUser = userRepository.findByEmail("admin@system.com");
        assertTrue(adminUser.isPresent());
        User admin = adminUser.get();
        assertEquals("System", admin.getFirstName());
        assertTrue(admin.getRoles().contains(adminRole.get()));
        
        // Verify Certificate Types Seed
        assertTrue(certificateTypeRepository.count() >= 2);
    }
}


