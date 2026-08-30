package com.carmats.user.controller;

import com.carmats.config.security.CustomUserDetails;
import com.carmats.user.dto.request.CreateAddressRequest;
import com.carmats.user.dto.request.SetDefaultAddressRequest;
import com.carmats.user.dto.request.UpdateAddressRequest;
import com.carmats.user.dto.response.AddressResponse;
import com.carmats.user.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Kullanıcı Adresleri", description = "Teslimat ve fatura adresi yönetimi")
@RestController
@RequestMapping("/api/v1/users/addresses")
@Validated
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @Operation(summary = "Kullanıcının adreslerini listeler", description = "Oturum açmış kullanıcının tüm kayıtlı adreslerini getirir.")
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAddresses(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(addressService.getUserAddresses(userDetails.getId()));
    }

    @Operation(summary = "Adres detayını getirir", description = "Belirtilen ID'ye sahip adresin detaylarını getirir.")
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> getAddressById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(addressService.getAddressById(userDetails.getId(), id));
    }

    @Operation(summary = "Yeni adres ekler", description = "Kullanıcı için yeni bir teslimat veya fatura adresi oluşturur.")
    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateAddressRequest request
    ) {
        AddressResponse response = addressService.createAddress(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Adres günceller", description = "Mevcut adres bilgilerini günceller.")
    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> updateAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAddressRequest request
    ) {
        return ResponseEntity.ok(addressService.updateAddress(userDetails.getId(), id, request));
    }

    @Operation(summary = "Adres siler", description = "Belirtilen adresi siler.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id
    ) {
        addressService.deleteAddress(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Varsayılan adres durumunu günceller", description = "Adresi varsayılan teslimat veya fatura adresi olarak ayarlar.")
    @PatchMapping("/{id}/default")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @RequestBody SetDefaultAddressRequest request
    ) {
        return ResponseEntity.ok(addressService.setDefaultAddress(userDetails.getId(), id, request));
    }
}
