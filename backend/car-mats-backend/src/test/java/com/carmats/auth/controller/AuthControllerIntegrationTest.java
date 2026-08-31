package com.carmats.auth.controller;

import com.carmats.config.security.JwtService;
import com.carmats.user.entity.Role;
import com.carmats.user.entity.User;
import com.carmats.user.repository.RoleRepository;
import com.carmats.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @org.junit.jupiter.api.BeforeEach
    void setUpAdminUser() {
        userRepository.findByEmailWithRoles("admin@carmats.local").ifPresentOrElse(
                admin -> {
                    admin.setPasswordHash(passwordEncoder.encode("Admin123!"));
                    admin.setActive(true);
                    roleRepository.findByName(Role.ROLE_ADMIN).ifPresent(admin::addRole);
                    userRepository.save(admin);
                },
                () -> {
                    User admin = new User(
                            "admin@carmats.local",
                            passwordEncoder.encode("Admin123!"),
                            "Admin",
                            "Sistem",
                            "+905551234567"
                    );
                    roleRepository.findByName(Role.ROLE_ADMIN).ifPresent(admin::addRole);
                    userRepository.save(admin);
                }
        );
    }

    @Test
    @DisplayName("POST /api/v1/auth/register registers new user")
    void shouldRegisterNewUser() throws Exception {
        String uniqueEmail = "testuser." + UUID.randomUUID().toString().substring(0, 8) + "@carmats.local";
        String requestJson = String.format("""
                {
                    "email": "%s",
                    "password": "Password123!",
                    "firstName": "Can",
                    "lastName": "Yıldız",
                    "phoneNumber": "+905559876543"
                }
                """, uniqueEmail);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.user.email", is(uniqueEmail)))
                .andExpect(jsonPath("$.user.roles", hasItem("ROLE_CUSTOMER")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login logs in seeded admin")
    void shouldLoginSeededAdmin() throws Exception {
        String requestJson = """
                {
                    "email": "admin@carmats.local",
                    "password": "Admin123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.user.email", is("admin@carmats.local")))
                .andExpect(jsonPath("$.user.roles", hasItem("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login fails on incorrect password")
    void shouldFailOnIncorrectPassword() throws Exception {
        String requestJson = """
                {
                    "email": "admin@carmats.local",
                    "password": "WrongPassword!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_CREDENTIALS")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh refreshes token")
    void shouldRefreshToken() throws Exception {
        // First login
        String loginJson = """
                {
                    "email": "admin@carmats.local",
                    "password": "Admin123!"
                }
                """;

        String loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(loginResult);
        String refreshToken = jsonNode.get("refreshToken").asText();

        // Refresh
        String refreshJson = String.format("""
                {
                    "refreshToken": "%s"
                }
                """, refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.user.email", is("admin@carmats.local")));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me returns current user info")
    void shouldReturnCurrentUserInfo() throws Exception {
        String loginJson = """
                {
                    "email": "admin@carmats.local",
                    "password": "Admin123!"
                }
                """;

        String loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(loginResult);
        String accessToken = jsonNode.get("accessToken").asText();

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("admin@carmats.local")))
                .andExpect(jsonPath("$.roles", hasItem("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Protected admin endpoint returns 401 when no token is provided")
    void shouldReturn401OnAdminWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("UNAUTHORIZED")));
    }

    @Test
    @DisplayName("Protected admin endpoint returns 403 when customer token is provided")
    void shouldReturn403OnAdminWithCustomerToken() throws Exception {
        String token = jwtService.generateAccessToken(
                UUID.randomUUID(),
                "customer@carmats.local",
                List.of("ROLE_CUSTOMER")
        );

        mockMvc.perform(get("/api/v1/admin/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("ACCESS_DENIED")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login sets HttpOnly refresh token cookie")
    void shouldSetHttpOnlyCookieOnLogin() throws Exception {
        String requestJson = """
                {
                    "email": "admin@carmats.local",
                    "password": "Admin123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("carmats_refresh_token"))
                .andExpect(cookie().httpOnly("carmats_refresh_token", true))
                .andExpect(cookie().path("carmats_refresh_token", "/api/v1/auth"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh works with HttpOnly cookie alone")
    void shouldRefreshWithCookieAlone() throws Exception {
        String loginJson = """
                {
                    "email": "admin@carmats.local",
                    "password": "Admin123!"
                }
                """;

        var loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        jakarta.servlet.http.Cookie refreshCookie = loginResult.getResponse().getCookie("carmats_refresh_token");
        org.junit.jupiter.api.Assertions.assertNotNull(refreshCookie);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refreshCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(cookie().exists("carmats_refresh_token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout revokes refresh token and clears cookie")
    void shouldLogoutAndRevokeRefreshToken() throws Exception {
        String loginJson = """
                {
                    "email": "admin@carmats.local",
                    "password": "Admin123!"
                }
                """;

        var loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        jakarta.servlet.http.Cookie refreshCookie = loginResult.getResponse().getCookie("carmats_refresh_token");
        org.junit.jupiter.api.Assertions.assertNotNull(refreshCookie);

        // Logout
        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(refreshCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("carmats_refresh_token", 0));

        // Refresh with revoked token should now fail
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refreshCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_REFRESH_TOKEN")));
    }

    @Test
    @DisplayName("Using rotated refresh token a second time fails")
    void shouldFailWhenReusingRotatedToken() throws Exception {
        String loginJson = """
                {
                    "email": "admin@carmats.local",
                    "password": "Admin123!"
                }
                """;

        var loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        jakarta.servlet.http.Cookie originalCookie = loginResult.getResponse().getCookie("carmats_refresh_token");

        // First refresh succeeds (and revokes original)
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(originalCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Reusing original revoked token fails
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(originalCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_REFRESH_TOKEN")));
    }

    @Test
    @DisplayName("Protected admin endpoint returns 200 when admin token is provided")
    void shouldReturn200OnAdminWithAdminToken() throws Exception {
        String token = jwtService.generateAccessToken(
                UUID.randomUUID(),
                "admin@carmats.local",
                List.of("ROLE_ADMIN")
        );

        mockMvc.perform(get("/api/v1/admin/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }
}
