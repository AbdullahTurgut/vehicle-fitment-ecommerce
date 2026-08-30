package com.carmats.catalog.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogCompatibleProductsIntegrationTest {

    private static final String PASSAT_B8_VARIANT_ID = "cccccccc-cccc-cccc-cccc-cccccccccccc";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/catalog/compatible-products returns compatible products with primary images for valid variant and year")
    void shouldReturnCompatibleProductsWithPrimaryImages() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/compatible-products")
                        .param("variantId", PASSAT_B8_VARIANT_ID)
                        .param("year", "2021")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].name", is("Volkswagen Passat B8 3D Bagaj Havuzu")))
                .andExpect(jsonPath("$[0].primaryImageUrl", is("/images/products/passat-b8-bagaj-havuzu.jpg")))
                .andExpect(jsonPath("$[0].effectivePrice", is(1499.90)))
                .andExpect(jsonPath("$[0].inStock", is(true)))
                .andExpect(jsonPath("$[1].name", is("Volkswagen Passat B8 3D Havuzlu Paspas")))
                .andExpect(jsonPath("$[1].primaryImageUrl", is("/images/products/passat-b8-paspas.jpg")))
                .andExpect(jsonPath("$[1].effectivePrice", is(2249.90)))
                .andExpect(jsonPath("$[1].inStock", is(true)))
                .andExpect(jsonPath("$[1].featured", is(true)));
    }

    @Test
    @DisplayName("GET /api/v1/catalog/compatible-products supports optional null year")
    void shouldReturnCompatibleProductsWithoutYear() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/compatible-products")
                        .param("variantId", PASSAT_B8_VARIANT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].primaryImageUrl", notNullValue()))
                .andExpect(jsonPath("$[1].primaryImageUrl", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/v1/catalog/compatible-products returns 400 when vehicle year is before generation start year")
    void shouldReturn400ForYearBeforeGenerationStart() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/compatible-products")
                        .param("variantId", PASSAT_B8_VARIANT_ID)
                        .param("year", "2010")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_VEHICLE_YEAR")))
                .andExpect(jsonPath("$.message", is("Seçilen yıl bu araç kasası için geçerli değildir.")));
    }

    @Test
    @DisplayName("GET /api/v1/catalog/compatible-products returns 400 when vehicle year is after generation end year")
    void shouldReturn400ForYearAfterGenerationEnd() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/compatible-products")
                        .param("variantId", PASSAT_B8_VARIANT_ID)
                        .param("year", "2025")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_VEHICLE_YEAR")))
                .andExpect(jsonPath("$.message", is("Seçilen yıl bu araç kasası için geçerli değildir.")));
    }

    @Test
    @DisplayName("GET /api/v1/catalog/compatible-products returns 404 when vehicle variant does not exist")
    void shouldReturn404ForNonExistentVariant() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/compatible-products")
                        .param("variantId", "99999999-9999-9999-9999-999999999999")
                        .param("year", "2021")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("VEHICLE_VARIANT_NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("Araç varyantı bulunamadı.")));
    }

    @Test
    @DisplayName("GET /api/v1/catalog/compatible-products returns 400 when year violates controller validation constraints")
    void shouldReturn400ForInvalidQueryParamYear() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/compatible-products")
                        .param("variantId", PASSAT_B8_VARIANT_ID)
                        .param("year", "1800")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")));
    }
}
