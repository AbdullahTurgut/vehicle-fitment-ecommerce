package com.carmats.catalog.service;

import com.carmats.catalog.dto.request.CreateCategoryRequest;
import com.carmats.catalog.dto.request.UpdateCategoryRequest;
import com.carmats.catalog.dto.request.UpdateCategoryStatusRequest;
import com.carmats.catalog.dto.response.AdminCategoryResponse;
import com.carmats.catalog.entity.Category;
import com.carmats.catalog.mapper.ProductMapper;
import com.carmats.catalog.repository.CategoryRepository;
import com.carmats.catalog.util.SlugUtils;
import com.carmats.common.exception.BusinessException;
import com.carmats.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;

    public AdminCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminCategoryResponse> getCategories() {
        return categoryRepository
                .findAllByOrderBySortOrderAscNameAsc()
                .stream()
                .map(ProductMapper::toAdminCategoryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminCategoryResponse getCategoryById(UUID id) {
        Category category = categoryRepository
                .findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "CATEGORY_NOT_FOUND",
                                "Kategori bulunamadı."
                        )
                );

        return ProductMapper.toAdminCategoryResponse(category);
    }

    public AdminCategoryResponse createCategory(CreateCategoryRequest request) {
        String slug = resolveSlug(request.slug(), request.name());

        if (categoryRepository.existsBySlug(slug)) {
            throw new BusinessException(
                    "CATEGORY_SLUG_ALREADY_EXISTS",
                    "Bu kategori slug değeri zaten kullanılıyor."
            );
        }

        Category parent = null;
        if (request.parentId() != null) {
            parent = categoryRepository
                    .findById(request.parentId())
                    .orElseThrow(() ->
                            new NotFoundException(
                                    "PARENT_CATEGORY_NOT_FOUND",
                                    "Üst kategori bulunamadı."
                            )
                    );
        }

        boolean active = request.active() == null || request.active();
        int sortOrder = request.sortOrder() != null ? request.sortOrder() : 0;

        Category category = new Category(
                parent,
                request.name().trim(),
                slug,
                request.description(),
                request.imageUrl(),
                active,
                sortOrder
        );

        Category saved = categoryRepository.save(category);
        return ProductMapper.toAdminCategoryResponse(saved);
    }

    public AdminCategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        Category category = categoryRepository
                .findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "CATEGORY_NOT_FOUND",
                                "Kategori bulunamadı."
                        )
                );

        String slug = resolveSlug(request.slug(), request.name());

        if (categoryRepository.existsBySlugAndIdNot(slug, id)) {
            throw new BusinessException(
                    "CATEGORY_SLUG_ALREADY_EXISTS",
                    "Bu kategori slug değeri zaten kullanılıyor."
            );
        }

        Category parent = null;
        if (request.parentId() != null) {
            if (request.parentId().equals(id)) {
                throw new BusinessException(
                        "INVALID_PARENT_CATEGORY",
                        "Kategori kendisini üst kategori olarak seçemez."
                );
            }

            parent = categoryRepository
                    .findById(request.parentId())
                    .orElseThrow(() ->
                            new NotFoundException(
                                    "PARENT_CATEGORY_NOT_FOUND",
                                    "Üst kategori bulunamadı."
                            )
                    );
        }

        int sortOrder = request.sortOrder() != null ? request.sortOrder() : category.getSortOrder();

        category.update(
                parent,
                request.name().trim(),
                slug,
                request.description(),
                request.imageUrl(),
                sortOrder
        );

        return ProductMapper.toAdminCategoryResponse(category);
    }

    public AdminCategoryResponse updateCategoryStatus(UUID id, UpdateCategoryStatusRequest request) {
        Category category = categoryRepository
                .findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "CATEGORY_NOT_FOUND",
                                "Kategori bulunamadı."
                        )
                );

        category.setActive(request.active());
        return ProductMapper.toAdminCategoryResponse(category);
    }

    private String resolveSlug(String providedSlug, String name) {
        String slugCandidate = (providedSlug != null && !providedSlug.isBlank())
                ? providedSlug
                : name;

        String slug = SlugUtils.toSlug(slugCandidate);
        if (slug.isBlank()) {
            throw new BusinessException(
                    "VALIDATION_ERROR",
                    "Geçerli bir kategori slug değeri oluşturulamadı."
            );
        }

        return slug;
    }
}
