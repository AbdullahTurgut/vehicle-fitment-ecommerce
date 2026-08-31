package com.carmats.user.controller;

import com.carmats.config.security.JwtService;
import com.carmats.user.entity.Role;
import com.carmats.user.entity.User;
import com.carmats.user.repository.AddressRepository;
import com.carmats.user.repository.RoleRepository;
import com.carmats.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AddressControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User testUser;
    private String token;

    @BeforeEach
    void setUp() {
        String email = "addr.test." + UUID.randomUUID().toString().substring(0, 8) + "@carmats.local";
        testUser = new User(
                email,
                passwordEncoder.encode("Pass123!"),
                "Mert",
                "Öztürk",
                "+905557778899"
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
    @DisplayName("Complete address CRUD and default address flow")
    void shouldPerformAddressCrudFlow() throws Exception {
        // 1. Create Address
        String createJson = """
                {
                    "title": "Ev Adresim",
                    "firstName": "Mert",
                    "lastName": "Öztürk",
                    "phoneNumber": "+905557778899",
                    "city": "Ankara",
                    "district": "Çankaya",
                    "neighborhood": "Kızılay",
                    "addressLine": "Atatürk Bulvarı No:123 D:4",
                    "postalCode": "06420"
                }
                """;

        String createResponse = mockMvc.perform(post("/api/v1/users/addresses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Ev Adresim")))
                .andExpect(jsonPath("$.city", is("Ankara")))
                .andExpect(jsonPath("$.defaultDelivery", is(true)))
                .andExpect(jsonPath("$.defaultBilling", is(true)))
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(createResponse);
        String addressId = jsonNode.get("id").asText();

        // 2. List Addresses
        mockMvc.perform(get("/api/v1/users/addresses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(addressId)));

        // 3. Get Address by ID
        mockMvc.perform(get("/api/v1/users/addresses/{id}", addressId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(addressId)))
                .andExpect(jsonPath("$.district", is("Çankaya")));

        // 4. Update Address
        String updateJson = """
                {
                    "title": "Evim Güncel",
                    "firstName": "Mert",
                    "lastName": "Öztürk",
                    "phoneNumber": "+905557778899",
                    "city": "Ankara",
                    "district": "Çankaya",
                    "neighborhood": "Kızılay",
                    "addressLine": "Atatürk Bulvarı No:123 D:8",
                    "postalCode": "06420"
                }
                """;

        mockMvc.perform(put("/api/v1/users/addresses/{id}", addressId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Evim Güncel")))
                .andExpect(jsonPath("$.addressLine", is("Atatürk Bulvarı No:123 D:8")));

        // 5. Delete Address
        mockMvc.perform(delete("/api/v1/users/addresses/{id}", addressId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // 6. Verify Deleted
        mockMvc.perform(get("/api/v1/users/addresses/{id}", addressId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Accessing another user's address returns 404")
    void shouldReturn404ForAnotherUserAddress() throws Exception {
        // Create another user's address
        User otherUser = new User("other." + UUID.randomUUID().toString().substring(0, 8) + "@carmats.local", "hash", "Diğer", "Kullanıcı", "+905551112233");
        otherUser = userRepository.save(otherUser);

        String otherToken = jwtService.generateAccessToken(otherUser.getId(), otherUser.getEmail(), List.of("ROLE_CUSTOMER"));

        String createJson = """
                {
                    "title": "Diğer Adres",
                    "firstName": "Diğer",
                    "lastName": "Kullanıcı",
                    "phoneNumber": "+905551112233",
                    "city": "Bursa",
                    "district": "Nilüfer",
                    "addressLine": "Özlüce Mah. No:1"
                }
                """;

        String createResponse = mockMvc.perform(post("/api/v1/users/addresses")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String otherAddressId = objectMapper.readTree(createResponse).get("id").asText();

        // Try to access other user's address with testUser's token
        mockMvc.perform(get("/api/v1/users/addresses/{id}", otherAddressId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("ADDRESS_NOT_FOUND")));
    }
}
