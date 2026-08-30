package com.carmats.user.service;

import com.carmats.common.exception.NotFoundException;
import com.carmats.user.dto.request.CreateAddressRequest;
import com.carmats.user.dto.request.SetDefaultAddressRequest;
import com.carmats.user.dto.request.UpdateAddressRequest;
import com.carmats.user.dto.response.AddressResponse;
import com.carmats.user.entity.Address;
import com.carmats.user.entity.User;
import com.carmats.user.repository.AddressRepository;
import com.carmats.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressService addressService;

    private User testUser;
    private Address testAddress;
    private UUID userId;
    private UUID addressId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        addressId = UUID.randomUUID();

        testUser = new User("user@carmats.local", "hash", "Ahmet", "Yılmaz", "+905551112233");
        testAddress = new Address(
                testUser,
                "Ev",
                "Ahmet",
                "Yılmaz",
                "+905551112233",
                "İstanbul",
                "Kadıköy",
                "Moda",
                "Caferağa Mah. Moda Cad. No:1",
                "34710",
                null,
                null,
                null,
                true,
                true
        );
    }

    @Test
    @DisplayName("Should return user addresses")
    void shouldReturnUserAddresses() {
        when(addressRepository.findAllByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(testAddress));

        List<AddressResponse> addresses = addressService.getUserAddresses(userId);

        assertThat(addresses).hasSize(1);
        assertThat(addresses.get(0).title()).isEqualTo("Ev");
        assertThat(addresses.get(0).city()).isEqualTo("İstanbul");
    }

    @Test
    @DisplayName("Should create first address as default delivery and billing")
    void shouldCreateFirstAddressAsDefault() {
        CreateAddressRequest request = new CreateAddressRequest(
                "İş",
                "Ahmet",
                "Yılmaz",
                "+905551112233",
                "İstanbul",
                "Şişli",
                "Mecidiyeköy",
                "Büyükdere Cad. No:100",
                "34381",
                null,
                null,
                null,
                false,
                false
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(addressRepository.countByUserId(userId)).thenReturn(0L);
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        AddressResponse response = addressService.createAddress(userId, request);

        assertThat(response.title()).isEqualTo("İş");
        assertThat(response.defaultDelivery()).isTrue();
        assertThat(response.defaultBilling()).isTrue();
    }

    @Test
    @DisplayName("Should update address")
    void shouldUpdateAddress() {
        UpdateAddressRequest request = new UpdateAddressRequest(
                "Yeni Ev",
                "Ahmet",
                "Yılmaz",
                "+905551112233",
                "İzmir",
                "Karşıyaka",
                "Bostanlı",
                "Cemal Gürsel Cad. No:50",
                "35540",
                null,
                null,
                null,
                false,
                false
        );

        when(addressRepository.findByIdAndUserId(addressId, userId)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        AddressResponse response = addressService.updateAddress(userId, addressId, request);

        assertThat(response.title()).isEqualTo("Yeni Ev");
        assertThat(response.city()).isEqualTo("İzmir");
        assertThat(response.district()).isEqualTo("Karşıyaka");
    }

    @Test
    @DisplayName("Should delete address")
    void shouldDeleteAddress() {
        when(addressRepository.findByIdAndUserId(addressId, userId)).thenReturn(Optional.of(testAddress));

        addressService.deleteAddress(userId, addressId);

        verify(addressRepository).delete(testAddress);
    }

    @Test
    @DisplayName("Should throw NotFoundException when address does not belong to user")
    void shouldThrowWhenAddressNotFound() {
        when(addressRepository.findByIdAndUserId(addressId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.deleteAddress(userId, addressId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", "ADDRESS_NOT_FOUND");
    }

    @Test
    @DisplayName("Should set default address")
    void shouldSetDefaultAddress() {
        SetDefaultAddressRequest request = new SetDefaultAddressRequest(true, false);
        when(addressRepository.findByIdAndUserId(addressId, userId)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        AddressResponse response = addressService.setDefaultAddress(userId, addressId, request);

        assertThat(response.defaultDelivery()).isTrue();
        verify(addressRepository).resetDefaultDelivery(userId);
    }
}
