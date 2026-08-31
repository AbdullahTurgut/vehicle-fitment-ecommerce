package com.carmats.user.controller;

import com.carmats.config.security.JwtService;
import com.carmats.user.entity.Role;
import com.carmats.user.entity.User;
import com.carmats.user.repository.RoleRepository;
import com.carmats.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User testUser;
    private String token;

    @BeforeEach
    void setUp() {
        String email = "profile.test." + UUID.randomUUID().toString().substring(0, 8) + "@carmats.local";
        testUser = new User(
                email,
                passwordEncoder.encode("CurrentPass123!"),
                "Burak",
                "Yılmaz",
                "+905554443322"
        );
        roleRepository.findByName(Role.ROLE_CUSTOMER).ifPresent(testUser::addRole);
        testUser = userRepository.save(testUser);

        token = jwtService.generateAccessToken(
                testUser.getId(),
                testUser.getEmail(),
                List.of("ROLE_CUSTOMER")
        );
    }

    @Test
    @DisplayName("GET /api/v1/users/profile returns user profile")
    void shouldGetProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUser.getId().toString())))
                .andExpect(jsonPath("$.email", is(testUser.getEmail())))
                .andExpect(jsonPath("$.firstName", is("Burak")))
                .andExpect(jsonPath("$.lastName", is("Yılmaz")));
    }

    @Test
    @DisplayName("PUT /api/v1/users/profile updates user profile")
    void shouldUpdateProfile() throws Exception {
        String requestJson = """
                {
                    "firstName": "Burak Can",
                    "lastName": "Yılmaz",
                    "phoneNumber": "+905559998877"
                }
                """;

        mockMvc.perform(put("/api/v1/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Burak Can")))
                .andExpect(jsonPath("$.phoneNumber", is("+905559998877")));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/password changes password")
    void shouldChangePassword() throws Exception {
        String requestJson = """
                {
                    "currentPassword": "CurrentPass123!",
                    "newPassword": "NewSuperPassword123!"
                }
                """;

        mockMvc.perform(patch("/api/v1/users/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/v1/users/password fails with wrong current password")
    void shouldFailOnWrongCurrentPassword() throws Exception {
        String requestJson = """
                {
                    "currentPassword": "WrongCurrentPassword!",
                    "newPassword": "NewSuperPassword123!"
                }
                """;

        mockMvc.perform(patch("/api/v1/users/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_CURRENT_PASSWORD")));
    }
}
