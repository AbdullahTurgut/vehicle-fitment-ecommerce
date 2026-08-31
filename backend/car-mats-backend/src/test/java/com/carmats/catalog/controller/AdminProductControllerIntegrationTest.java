package com.carmats.catalog.controller;

import com.carmats.catalog.entity.Category;
import com.carmats.catalog.entity.Product;
import com.carmats.catalog.entity.ProductStatus;
import com.carmats.catalog.repository.CategoryRepository;
import com.carmats.catalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(authorities = "ROLE_ADMIN")
class AdminProductControllerIntegrationTest {

    private static final UUID SEEDED_CATEGORY_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID SEEDED_PRODUCT_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID SEEDED_VARIANT_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"); // Passat B8 Variant

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private com.carmats.shipping.repository.ShipmentRepository shipmentRepository;

    @Autowired
    private com.carmats.payment.repository.PaymentRepository paymentRepository;

    @Autowired
    private com.carmats.review.repository.ProductReviewRepository reviewRepository;

    @Autowired
    private com.carmats.favorite.repository.FavoriteRepository favoriteRepository;

    @Autowired
    private com.carmats.order.repository.OrderRepository orderRepository;

    @Autowired
    private com.carmats.cart.repository.CartRepository cartRepository;

    @BeforeEach
    void resetState() {
        reviewRepository.deleteAll();
        favoriteRepository.deleteAll();
        shipmentRepository.deleteAll();
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        cartRepository.deleteAll();

        // Restore seeded product to pristine initial seed state
        productRepository.findById(SEEDED_PRODUCT_ID).ifPresent(p -> {
            Category cat = categoryRepository.findById(SEEDED_CATEGORY_ID).orElseThrow();
            p.update(
                    cat,
                    "Volkswagen Passat B8 3D Havuzlu Paspas",
                    "volkswagen-passat-b8-3d-havuzlu-paspas",
                    "PASSAT-B8-PASPAS-001",
                    "Volkswagen Passat B8 araçlara özel 3D havuzlu paspas.",
                    "Aracınıza özel ölçülerde tasarlanan, yüksek kenarlı ve kolay temizlenebilir 3D havuzlu paspas.",
                    new BigDecimal("2499.90"),
                    new BigDecimal("2249.90"),
                    25,
                    true,
                    "CarMats",
                    "TPE"
            );
            p.setStatus(ProductStatus.ACTIVE);
            productRepository.save(p);
        });

        // Clean up test products
        productRepository.findAll().stream()
                .filter(p -> !p.getId().equals(SEEDED_PRODUCT_ID) && !p.getId().equals(UUID.fromString("20000000-0000-0000-0000-000000000002")))
                .forEach(productRepository::delete);
    }

    private Product createTemporaryProduct(String sku) {
        Category cat = categoryRepository.findById(SEEDED_CATEGORY_ID).orElseThrow();
        Product product = new Product(
                cat,
                "Geçici Ürün " + sku,
                "gecici-urun-" + sku.toLowerCase(),
                sku,
                "Kısa açıklama",
                "Detaylı açıklama",
                new BigDecimal("1500.00"),
                new BigDecimal("1200.00"),
                20,
                ProductStatus.ACTIVE,
                false,
                "Marka",
                "TPE"
        );
        return productRepository.save(product);
    }

    @Test
    @DisplayName("GET /api/v1/admin/products returns paginated products")
    void shouldListProducts() throws Exception {
        mockMvc.perform(get("/api/v1/admin/products")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[0].id", notNullValue()))
                .andExpect(jsonPath("$.content[0].name", notNullValue()))
                .andExpect(jsonPath("$.content[0].sku", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/v1/admin/products/{id} returns full product detail")
    void shouldGetProductById() throws Exception {
        mockMvc.perform(get("/api/v1/admin/products/{id}", SEEDED_PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(SEEDED_PRODUCT_ID.toString())))
                .andExpect(jsonPath("$.name", is("Volkswagen Passat B8 3D Havuzlu Paspas")))
                .andExpect(jsonPath("$.sku", is("PASSAT-B8-PASPAS-001")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.images", notNullValue()))
                .andExpect(jsonPath("$.features", notNullValue()))
                .andExpect(jsonPath("$.compatibilities", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/admin/products creates a new product")
    void shouldCreateProduct() throws Exception {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String uniqueSku = "SKU-TEST-" + uniqueSuffix;
        String uniqueName = "Yeni Test Ürünü " + uniqueSuffix;
        String requestJson = String.format("""
                {
                    "categoryId": "%s",
                    "name": "%s",
                    "sku": "%s",
                    "basePrice": 1250.00,
                    "salePrice": 990.00,
                    "stockQuantity": 30,
                    "status": "DRAFT",
                    "featured": false,
                    "manufacturerBrand": "Test Marka",
                    "material": "TPE"
                }
                """, SEEDED_CATEGORY_ID, uniqueName, uniqueSku);

        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(uniqueName)))
                .andExpect(jsonPath("$.sku", is(uniqueSku)))
                .andExpect(jsonPath("$.status", is("DRAFT")))
                .andExpect(jsonPath("$.stockQuantity", is(30)));
    }

    @Test
    @DisplayName("POST /api/v1/admin/products returns 400 on duplicate SKU")
    void shouldReturn400OnDuplicateSku() throws Exception {
        String requestJson = String.format("""
                {
                    "categoryId": "%s",
                    "name": "Tekrarlanan SKU Testi",
                    "sku": "PASSAT-B8-PASPAS-001",
                    "basePrice": 1250.00,
                    "stockQuantity": 10
                }
                """, SEEDED_CATEGORY_ID);

        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("PRODUCT_SKU_ALREADY_EXISTS")));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/products/{id} updates existing product")
    void shouldUpdateProduct() throws Exception {
        Product temp = createTemporaryProduct("SKU-PUT-" + UUID.randomUUID().toString().substring(0, 6));

        String requestJson = String.format("""
                {
                    "categoryId": "%s",
                    "name": "Geçici Ürün Güncel",
                    "slug": "gecici-urun-guncel",
                    "sku": "%s",
                    "shortDescription": "Güncellenmiş kısa açıklama",
                    "description": "Güncellenmiş detaylı açıklama",
                    "basePrice": 1950.00,
                    "salePrice": 1690.00,
                    "stockQuantity": 60,
                    "featured": true,
                    "manufacturerBrand": "Sahler",
                    "material": "TPE"
                }
                """, SEEDED_CATEGORY_ID, temp.getSku());

        mockMvc.perform(put("/api/v1/admin/products/{id}", temp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(temp.getId().toString())))
                .andExpect(jsonPath("$.name", is("Geçici Ürün Güncel")))
                .andExpect(jsonPath("$.stockQuantity", is(60)));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/products/{id}/status updates product status")
    void shouldUpdateProductStatus() throws Exception {
        Product temp = createTemporaryProduct("SKU-STATUS-" + UUID.randomUUID().toString().substring(0, 6));

        String requestJson = """
                {
                    "status": "PASSIVE"
                }
                """;

        mockMvc.perform(patch("/api/v1/admin/products/{id}/status", temp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(temp.getId().toString())))
                .andExpect(jsonPath("$.status", is("PASSIVE")));
    }

    @Test
    @DisplayName("POST /api/v1/admin/products/{id}/images and DELETE image")
    void shouldAddAndDeleteProductImage() throws Exception {
        Product temp = createTemporaryProduct("SKU-IMG-" + UUID.randomUUID().toString().substring(0, 6));

        String addImageJson = """
                {
                    "url": "https://img.carmats.local/products/test-img.jpg",
                    "altText": "Test Görseli",
                    "sortOrder": 5,
                    "primary": false
                }
                """;

        String createResponse = mockMvc.perform(post("/api/v1/admin/products/{id}/images", temp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addImageJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.url", is("https://img.carmats.local/products/test-img.jpg")))
                .andReturn().getResponse().getContentAsString();

        // Extract ID and delete
        String imageId = createResponse.split("\"id\":\"")[1].split("\"")[0];

        mockMvc.perform(delete("/api/v1/admin/products/{id}/images/{imageId}", temp.getId(), imageId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/v1/admin/products/{id}/features and DELETE feature")
    void shouldAddAndDeleteProductFeature() throws Exception {
        Product temp = createTemporaryProduct("SKU-FEAT-" + UUID.randomUUID().toString().substring(0, 6));

        String addFeatureJson = """
                {
                    "title": "Test Özellik",
                    "description": "Açıklama detayı",
                    "icon": "shield-check",
                    "sortOrder": 10
                }
                """;

        String createResponse = mockMvc.perform(post("/api/v1/admin/products/{id}/features", temp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addFeatureJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Test Özellik")))
                .andReturn().getResponse().getContentAsString();

        String featureId = createResponse.split("\"id\":\"")[1].split("\"")[0];

        mockMvc.perform(delete("/api/v1/admin/products/{id}/features/{featureId}", temp.getId(), featureId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/v1/admin/products/{id}/compatibilities and DELETE compatibility")
    void shouldAddAndDeleteProductCompatibility() throws Exception {
        Product temp = createTemporaryProduct("SKU-COMPAT-" + UUID.randomUUID().toString().substring(0, 6));

        String addCompatJson = String.format("""
                {
                    "vehicleVariantId": "%s",
                    "startYear": 2016,
                    "endYear": 2019,
                    "notes": "Özel seri uyumlu"
                }
                """, SEEDED_VARIANT_ID);

        String createResponse = mockMvc.perform(post("/api/v1/admin/products/{id}/compatibilities", temp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addCompatJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.vehicleVariantId", is(SEEDED_VARIANT_ID.toString())))
                .andExpect(jsonPath("$.startYear", is(2016)))
                .andExpect(jsonPath("$.endYear", is(2019)))
                .andReturn().getResponse().getContentAsString();

        String compatId = createResponse.split("\"id\":\"")[1].split("\"")[0];

        mockMvc.perform(delete("/api/v1/admin/products/{id}/compatibilities/{compatibilityId}", temp.getId(), compatId))
                .andExpect(status().isNoContent());
    }
}
