package com.carmats.favorite.controller;

import com.carmats.common.response.PageResponse;
import com.carmats.config.security.CustomUserDetails;
import com.carmats.favorite.dto.response.FavoriteResponse;
import com.carmats.favorite.dto.response.FavoriteToggleResponse;
import com.carmats.favorite.service.FavoriteService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/favorites")
@PreAuthorize("isAuthenticated()")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{productId}")
    public ResponseEntity<FavoriteToggleResponse> toggleFavorite(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID productId
    ) {
        FavoriteToggleResponse response = favoriteService.toggleFavorite(userDetails.getId(), productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<FavoriteResponse>> getUserFavorites(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<FavoriteResponse> response = favoriteService.getUserFavorites(userDetails.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFavorite(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID productId
    ) {
        favoriteService.removeFavorite(userDetails.getId(), productId);
        return ResponseEntity.noContent().build();
    }
}
