package com.carmats.catalog.controller;

import com.carmats.catalog.entity.Category;
import com.carmats.catalog.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminCategoryControllerIntegrationTest {

    private static final UUID SEEDED_PASPAS_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID SEEDED_BAGAJ_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void resetState() {
        // Reset seeded categories to pristine initial state
        categoryRepository.findById(SEEDED_PASPAS_ID).ifPresent(cat -> {
            cat.update(null, "3D Oto Paspas", "3d-oto-paspas", null, null, 10);
            cat.setActive(true);
            categoryRepository.save(cat);
        });

        // Clean up test-created categories
        categoryRepository.findAll().stream()
                .filter(cat -> !cat.getId().equals(SEEDED_PASPAS_ID) && !cat.getId().equals(SEEDED_BAGAJ_ID))
                .forEach(categoryRepository::delete);
    }

    @Test
    @DisplayName("GET /api/v1/admin/categories returns all categories for admin")
    void shouldListAllCategories() throws Exception {
        mockMvc.perform(get("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].name", notNullValue()))
                .andExpect(jsonPath("$[0].slug", notNullValue()))
                .andExpect(jsonPath("$[0].active", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/v1/admin/categories/{id} returns full category details")
    void shouldGetCategoryById() throws Exception {
        mockMvc.perform(get("/api/v1/admin/categories/{id}", SEEDED_PASPAS_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(SEEDED_PASPAS_ID.toString())))
                .andExpect(jsonPath("$.name", is("3D Oto Paspas")))
                .andExpect(jsonPath("$.slug", is("3d-oto-paspas")))
                .andExpect(jsonPath("$.active", is(true)))
                .andExpect(jsonPath("$.sortOrder", is(10)));
    }

    @Test
    @DisplayName("GET /api/v1/admin/categories/{id} returns 404 when category does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/admin/categories/{id}", "99999999-9999-9999-9999-999999999999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("CATEGORY_NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("Kategori bulunamadı.")));
    }

    @Test
    @DisplayName("POST /api/v1/admin/categories creates new category successfully")
    void shouldCreateCategorySuccessfully() throws Exception {
        String requestJson = """
                {
                    "name": "Güneşlik ve Perdeler",
                    "description": "Araç içi özel perdeler ve güneşlik ürünleri",
                    "sortOrder": 30,
                    "active": true
                }
                """;

        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Güneşlik ve Perdeler")))
                .andExpect(jsonPath("$.slug", is("guneslik-ve-perdeler")))
                .andExpect(jsonPath("$.sortOrder", is(30)))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    @DisplayName("POST /api/v1/admin/categories returns 400 when duplicate slug provided")
    void shouldReturn400WhenDuplicateSlugOnCreate() throws Exception {
        String requestJson = """
                {
                    "name": "3D Oto Paspas Tekrar",
                    "slug": "3d-oto-paspas",
                    "sortOrder": 10
                }
                """;

        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("CATEGORY_SLUG_ALREADY_EXISTS")))
                .andExpect(jsonPath("$.message", is("Bu kategori slug değeri zaten kullanılıyor.")));
    }

    @Test
    @DisplayName("POST /api/v1/admin/categories returns 400 when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        String requestJson = """
                {
                    "name": ""
                }
                """;

        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.errors.name", notNullValue()));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/categories/{id} updates existing category")
    void shouldUpdateCategorySuccessfully() throws Exception {
        Category testCat = categoryRepository.save(new Category(null, "Geçici Kategori", "gecici-kategori", "Açıklama", null, true, 5));

        String requestJson = """
                {
                    "name": "Geçici Kategori Güncel",
                    "slug": "gecici-kategori-guncel",
                    "description": "Güncellenmiş açıklama",
                    "sortOrder": 12
                }
                """;

        mockMvc.perform(put("/api/v1/admin/categories/{id}", testCat.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCat.getId().toString())))
                .andExpect(jsonPath("$.name", is("Geçici Kategori Güncel")))
                .andExpect(jsonPath("$.slug", is("gecici-kategori-guncel")))
                .andExpect(jsonPath("$.description", is("Güncellenmiş açıklama")))
                .andExpect(jsonPath("$.sortOrder", is(12)));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/categories/{id}/status changes category active status")
    void shouldUpdateCategoryStatus() throws Exception {
        Category testCat = categoryRepository.save(new Category(null, "Status Test", "status-test", null, null, true, 5));

        String deactivateJson = """
                {
                    "active": false
                }
                """;

        mockMvc.perform(patch("/api/v1/admin/categories/{id}/status", testCat.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deactivateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCat.getId().toString())))
                .andExpect(jsonPath("$.active", is(false)));

        // Re-activate
        String reactivateJson = """
                {
                    "active": true
                }
                """;

        mockMvc.perform(patch("/api/v1/admin/categories/{id}/status", testCat.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reactivateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCat.getId().toString())))
                .andExpect(jsonPath("$.active", is(true)));
    }
}
