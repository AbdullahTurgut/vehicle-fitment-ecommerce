package com.carmats.config.security;

import com.carmats.user.entity.Role;
import com.carmats.user.entity.User;
import com.carmats.user.repository.RoleRepository;
import com.carmats.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminUserInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Value("${admin.initial.email:admin@carmats.local}")
    private String initialAdminEmail;

    @Value("${admin.initial.password:}")
    private String initialAdminPassword;

    @Value("${admin.initial.first-name:Admin}")
    private String initialFirstName;

    @Value("${admin.initial.last-name:Sistem}")
    private String initialLastName;

    public AdminUserInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            Environment environment
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        boolean adminExists = userRepository.existsByRoles_Name(Role.ROLE_ADMIN);
        if (adminExists) {
            log.info("Administrative account verified; skipping initial bootstrap.");
            return;
        }

        if (initialAdminPassword == null || initialAdminPassword.isBlank()) {
            if (environment.acceptsProfiles(Profiles.of("prod", "production"))) {
                throw new IllegalStateException(
                        "CRITICAL BOOTSTRAP FAILURE: No administrator account exists in database and ADMIN_INITIAL_PASSWORD is not configured."
                );
            }
            log.warn("Advisory: No administrative account detected and ADMIN_INITIAL_PASSWORD not configured (development mode).");
            return;
        }

        Role adminRole = roleRepository.findByName(Role.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(Role.ROLE_ADMIN, "Platform yönetici rolü")));
        Role customerRole = roleRepository.findByName(Role.ROLE_CUSTOMER)
                .orElseGet(() -> roleRepository.save(new Role(Role.ROLE_CUSTOMER, "Standart müşteri rolü")));

        User admin = new User(
                initialAdminEmail.trim().toLowerCase(),
                passwordEncoder.encode(initialAdminPassword),
                initialFirstName.trim(),
                initialLastName.trim(),
                null
        );
        admin.addRole(adminRole);
        admin.addRole(customerRole);
        userRepository.save(admin);

        log.info("Initial administrative account successfully initialized for: {}", initialAdminEmail.trim().toLowerCase());
    }
}
