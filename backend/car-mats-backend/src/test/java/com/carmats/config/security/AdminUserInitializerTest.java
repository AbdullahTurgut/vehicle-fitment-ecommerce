package com.carmats.config.security;

import com.carmats.user.entity.Role;
import com.carmats.user.entity.User;
import com.carmats.user.repository.RoleRepository;
import com.carmats.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Environment environment;

    @InjectMocks
    private AdminUserInitializer adminUserInitializer;

    @Test
    @DisplayName("When an admin already exists, do nothing and never overwrite credentials")
    void whenAdminAlreadyExists_doesNothing() {
        when(userRepository.existsByRoles_Name(Role.ROLE_ADMIN)).thenReturn(true);

        adminUserInitializer.run(null);

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("When no admin exists and bootstrap password provided, creates initial administrator")
    void whenNoAdminAndBootstrapPasswordProvided_createsAdmin() {
        when(userRepository.existsByRoles_Name(Role.ROLE_ADMIN)).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed-test-password");
        when(roleRepository.findByName(Role.ROLE_ADMIN)).thenReturn(Optional.of(new Role(Role.ROLE_ADMIN, "Admin")));
        when(roleRepository.findByName(Role.ROLE_CUSTOMER)).thenReturn(Optional.of(new Role(Role.ROLE_CUSTOMER, "Customer")));

        ReflectionTestUtils.setField(adminUserInitializer, "initialAdminEmail", "admin@carmats.local");
        ReflectionTestUtils.setField(adminUserInitializer, "initialAdminPassword", "BootstrapSecret123!");
        ReflectionTestUtils.setField(adminUserInitializer, "initialFirstName", "Admin");
        ReflectionTestUtils.setField(adminUserInitializer, "initialLastName", "Sistem");

        adminUserInitializer.run(null);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User created = userCaptor.getValue();
        assertThat(created.getEmail()).isEqualTo("admin@carmats.local");
        assertThat(created.getPasswordHash()).isEqualTo("hashed-test-password");
        assertThat(created.getRoles()).extracting(Role::getName).containsExactlyInAnyOrder(Role.ROLE_ADMIN, Role.ROLE_CUSTOMER);
    }

    @Test
    @DisplayName("When no admin exists and bootstrap password missing in production, fails fast")
    void whenNoAdminAndMissingPasswordInProduction_throwsIllegalStateException() {
        when(userRepository.existsByRoles_Name(Role.ROLE_ADMIN)).thenReturn(false);
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);

        ReflectionTestUtils.setField(adminUserInitializer, "initialAdminEmail", "admin@carmats.local");
        ReflectionTestUtils.setField(adminUserInitializer, "initialAdminPassword", "");

        assertThatThrownBy(() -> adminUserInitializer.run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CRITICAL BOOTSTRAP FAILURE");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("When no admin exists and bootstrap password missing in development, logs warning without throwing")
    void whenNoAdminAndMissingPasswordInDev_logsWarningWithoutThrowing() {
        when(userRepository.existsByRoles_Name(Role.ROLE_ADMIN)).thenReturn(false);
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false);

        ReflectionTestUtils.setField(adminUserInitializer, "initialAdminEmail", "admin@carmats.local");
        ReflectionTestUtils.setField(adminUserInitializer, "initialAdminPassword", "");

        adminUserInitializer.run(null);

        verify(userRepository, never()).save(any());
    }
}
