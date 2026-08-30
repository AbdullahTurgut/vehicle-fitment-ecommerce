package com.carmats.catalog.service;

import com.carmats.catalog.dto.request.CreateCategoryRequest;
import com.carmats.catalog.dto.request.UpdateCategoryRequest;
import com.carmats.catalog.dto.request.UpdateCategoryStatusRequest;
import com.carmats.catalog.dto.response.AdminCategoryResponse;
import com.carmats.catalog.entity.Category;
import com.carmats.catalog.repository.CategoryRepository;
import com.carmats.common.exception.BusinessException;
import com.carmats.common.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private AdminCategoryService adminCategoryService;

    @Test
    @DisplayName("Should return all categories for admin")
    void shouldReturnAllCategories() {
        Category cat1 = new Category(null, "Paspas", "paspas", "Açıklama 1", "/img1.jpg", true, 10);
        Category cat2 = new Category(null, "Bagaj", "bagaj", "Açıklama 2", "/img2.jpg", false, 20);

        when(categoryRepository.findAllByOrderBySortOrderAscNameAsc())
                .thenReturn(List.of(cat1, cat2));

        List<AdminCategoryResponse> result = adminCategoryService.getCategories();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Paspas");
        assertThat(result.get(0).slug()).isEqualTo("paspas");
        assertThat(result.get(0).active()).isTrue();
        assertThat(result.get(1).name()).isEqualTo("Bagaj");
        assertThat(result.get(1).active()).isFalse();
    }

    @Test
    @DisplayName("Should return category by id")
    void shouldReturnCategoryById() {
        UUID id = UUID.randomUUID();
        Category cat = new Category(null, "Paspas", "paspas", "Açıklama", "/img.jpg", true, 10);

        when(categoryRepository.findById(id)).thenReturn(Optional.of(cat));

        AdminCategoryResponse result = adminCategoryService.getCategoryById(id);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Paspas");
        assertThat(result.slug()).isEqualTo("paspas");
    }

    @Test
    @DisplayName("Should throw NotFoundException when category id not found")
    void shouldThrowWhenCategoryNotFound() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminCategoryService.getCategoryById(id))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", "CATEGORY_NOT_FOUND");
    }

    @Test
    @DisplayName("Should create category with explicit slug")
    void shouldCreateCategoryWithExplicitSlug() {
        CreateCategoryRequest request = new CreateCategoryRequest(
                null,
                "Yeni Kategori",
                "ozel-slug",
                "Açıklama",
                "/img.jpg",
                true,
                10
        );

        when(categoryRepository.existsBySlug("ozel-slug")).thenReturn(false);
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AdminCategoryResponse response = adminCategoryService.createCategory(request);

        assertThat(response.name()).isEqualTo("Yeni Kategori");
        assertThat(response.slug()).isEqualTo("ozel-slug");
        assertThat(response.active()).isTrue();
        assertThat(response.sortOrder()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should create category with auto-generated slug from Turkish name")
    void shouldCreateCategoryWithAutoSlug() {
        CreateCategoryRequest request = new CreateCategoryRequest(
                null,
                "Özel Şık 3D Araç Paspası",
                null,
                "Açıklama",
                null,
                null,
                null
        );

        when(categoryRepository.existsBySlug("ozel-sik-3d-arac-paspasi")).thenReturn(false);
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AdminCategoryResponse response = adminCategoryService.createCategory(request);

        assertThat(response.name()).isEqualTo("Özel Şık 3D Araç Paspası");
        assertThat(response.slug()).isEqualTo("ozel-sik-3d-arac-paspasi");
        assertThat(response.active()).isTrue();
        assertThat(response.sortOrder()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should throw BusinessException when creating category with duplicate slug")
    void shouldThrowWhenDuplicateSlugOnCreate() {
        CreateCategoryRequest request = new CreateCategoryRequest(
                null,
                "Paspas",
                "paspas",
                null,
                null,
                true,
                0
        );

        when(categoryRepository.existsBySlug("paspas")).thenReturn(true);

        assertThatThrownBy(() -> adminCategoryService.createCategory(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "CATEGORY_SLUG_ALREADY_EXISTS");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw NotFoundException when parent category does not exist on create")
    void shouldThrowWhenParentNotFoundOnCreate() {
        UUID parentId = UUID.randomUUID();
        CreateCategoryRequest request = new CreateCategoryRequest(
                parentId,
                "Alt Kategori",
                "alt-kategori",
                null,
                null,
                true,
                0
        );

        when(categoryRepository.existsBySlug("alt-kategori")).thenReturn(false);
        when(categoryRepository.findById(parentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminCategoryService.createCategory(request))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", "PARENT_CATEGORY_NOT_FOUND");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update category successfully")
    void shouldUpdateCategorySuccessfully() {
        UUID id = UUID.randomUUID();
        Category existing = new Category(null, "Eski İsim", "eski-slug", "Eski Açıklama", "/old.jpg", true, 5);

        UpdateCategoryRequest request = new UpdateCategoryRequest(
                null,
                "Güncel İsim",
                "guncel-slug",
                "Yeni Açıklama",
                "/new.jpg",
                15
        );

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsBySlugAndIdNot("guncel-slug", id)).thenReturn(false);

        AdminCategoryResponse response = adminCategoryService.updateCategory(id, request);

        assertThat(response.name()).isEqualTo("Güncel İsim");
        assertThat(response.slug()).isEqualTo("guncel-slug");
        assertThat(response.description()).isEqualTo("Yeni Açıklama");
        assertThat(response.imageUrl()).isEqualTo("/new.jpg");
        assertThat(response.sortOrder()).isEqualTo(15);
    }

    @Test
    @DisplayName("Should throw BusinessException when updating category with duplicate slug from another category")
    void shouldThrowWhenDuplicateSlugOnUpdate() {
        UUID id = UUID.randomUUID();
        Category existing = new Category(null, "Kategori", "kategori", null, null, true, 0);

        UpdateCategoryRequest request = new UpdateCategoryRequest(
                null,
                "Kategori",
                "baska-kategori",
                null,
                null,
                0
        );

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsBySlugAndIdNot("baska-kategori", id)).thenReturn(true);

        assertThatThrownBy(() -> adminCategoryService.updateCategory(id, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "CATEGORY_SLUG_ALREADY_EXISTS");
    }

    @Test
    @DisplayName("Should throw BusinessException when updating category to set itself as parent")
    void shouldThrowWhenSelfParentOnUpdate() {
        UUID id = UUID.randomUUID();
        Category existing = new Category(null, "Kategori", "kategori", null, null, true, 0);

        UpdateCategoryRequest request = new UpdateCategoryRequest(
                id,
                "Kategori",
                "kategori",
                null,
                null,
                0
        );

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> adminCategoryService.updateCategory(id, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_PARENT_CATEGORY");
    }

    @Test
    @DisplayName("Should update category status")
    void shouldUpdateCategoryStatus() {
        UUID id = UUID.randomUUID();
        Category existing = new Category(null, "Kategori", "kategori", null, null, true, 0);

        UpdateCategoryStatusRequest request = new UpdateCategoryStatusRequest(false);

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));

        AdminCategoryResponse response = adminCategoryService.updateCategoryStatus(id, request);

        assertThat(response.active()).isFalse();
        assertThat(existing.isActive()).isFalse();
    }
}
