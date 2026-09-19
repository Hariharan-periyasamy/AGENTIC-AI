package com.certificate.system.config;
import com.certificate.system.entity.CertificateType;
import com.certificate.system.entity.Role;
import com.certificate.system.entity.User;
import com.certificate.system.entity.enums.RoleName;
import com.certificate.system.repository.CertificateTypeRepository;
import com.certificate.system.repository.RoleRepository;
import com.certificate.system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);
    
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CertificateTypeRepository certificateTypeRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(RoleRepository roleRepository, UserRepository userRepository,
                          CertificateTypeRepository certificateTypeRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.certificateTypeRepository = certificateTypeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedAdminUser();
        seedCertificateTypes();
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            Role userRole = new Role();
            userRole.setName(RoleName.ROLE_USER);
            roleRepository.save(userRole);

            Role adminRole = new Role();
            adminRole.setName(RoleName.ROLE_ADMIN);
            roleRepository.save(adminRole);
            logger.info("Roles seeded successfully.");
        }
    }

    private void seedAdminUser() {
        if (userRepository.findByEmail("admin@system.com").isEmpty()) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin Role not found"));

            User admin = new User();
            admin.setEmail("admin@system.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("System");
            admin.setLastName("Admin");
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);

            userRepository.save(admin);
            logger.info("Default admin user seeded successfully.");
        }
    }

    private void seedCertificateTypes() {
        if (certificateTypeRepository.count() == 0) {
            CertificateType type1 = new CertificateType();
            type1.setName("Academic Transcript");
            type1.setDescription("Official academic records");
            certificateTypeRepository.save(type1);

            CertificateType type2 = new CertificateType();
            type2.setName("Degree Certificate");
            type2.setDescription("Official degree certificate");
            certificateTypeRepository.save(type2);
            
            logger.info("Certificate Types seeded successfully.");
        }
    }
}
