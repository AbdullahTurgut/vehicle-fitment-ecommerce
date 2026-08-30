package com.carmats.user.service;

import com.carmats.common.exception.NotFoundException;
import com.carmats.user.dto.request.CreateAddressRequest;
import com.carmats.user.dto.request.SetDefaultAddressRequest;
import com.carmats.user.dto.request.UpdateAddressRequest;
import com.carmats.user.dto.response.AddressResponse;
import com.carmats.user.entity.Address;
import com.carmats.user.entity.User;
import com.carmats.user.mapper.AddressMapper;
import com.carmats.user.repository.AddressRepository;
import com.carmats.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(UUID userId) {
        return addressRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(AddressMapper::toAddressResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AddressResponse getAddressById(UUID userId, UUID addressId) {
        Address address = findAddressByUserIdAndId(userId, addressId);
        return AddressMapper.toAddressResponse(address);
    }

    public AddressResponse createAddress(UUID userId, CreateAddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "USER_NOT_FOUND",
                                "Kullanıcı bulunamadı."
                        )
                );

        long addressCount = addressRepository.countByUserId(userId);

        boolean isDefaultDelivery = addressCount == 0 || Boolean.TRUE.equals(request.defaultDelivery());
        boolean isDefaultBilling = addressCount == 0 || Boolean.TRUE.equals(request.defaultBilling());

        if (isDefaultDelivery) {
            addressRepository.resetDefaultDelivery(userId);
        }
        if (isDefaultBilling) {
            addressRepository.resetDefaultBilling(userId);
        }

        Address address = new Address(
                user,
                request.title().trim(),
                request.firstName().trim(),
                request.lastName().trim(),
                request.phoneNumber().trim(),
                request.city().trim(),
                request.district().trim(),
                request.neighborhood() != null ? request.neighborhood().trim() : null,
                request.addressLine().trim(),
                request.postalCode() != null ? request.postalCode().trim() : null,
                request.companyName() != null ? request.companyName().trim() : null,
                request.taxNumber() != null ? request.taxNumber().trim() : null,
                request.taxOffice() != null ? request.taxOffice().trim() : null,
                isDefaultDelivery,
                isDefaultBilling
        );

        Address saved = addressRepository.save(address);
        return AddressMapper.toAddressResponse(saved);
    }

    public AddressResponse updateAddress(UUID userId, UUID addressId, UpdateAddressRequest request) {
        Address address = findAddressByUserIdAndId(userId, addressId);

        boolean isDefaultDelivery = Boolean.TRUE.equals(request.defaultDelivery());
        boolean isDefaultBilling = Boolean.TRUE.equals(request.defaultBilling());

        if (isDefaultDelivery && !address.isDefaultDelivery()) {
            addressRepository.resetDefaultDelivery(userId);
        }
        if (isDefaultBilling && !address.isDefaultBilling()) {
            addressRepository.resetDefaultBilling(userId);
        }

        address.update(
                request.title().trim(),
                request.firstName().trim(),
                request.lastName().trim(),
                request.phoneNumber().trim(),
                request.city().trim(),
                request.district().trim(),
                request.neighborhood() != null ? request.neighborhood().trim() : null,
                request.addressLine().trim(),
                request.postalCode() != null ? request.postalCode().trim() : null,
                request.companyName() != null ? request.companyName().trim() : null,
                request.taxNumber() != null ? request.taxNumber().trim() : null,
                request.taxOffice() != null ? request.taxOffice().trim() : null,
                isDefaultDelivery || address.isDefaultDelivery(),
                isDefaultBilling || address.isDefaultBilling()
        );

        Address updated = addressRepository.save(address);
        return AddressMapper.toAddressResponse(updated);
    }

    public void deleteAddress(UUID userId, UUID addressId) {
        Address address = findAddressByUserIdAndId(userId, addressId);
        addressRepository.delete(address);
    }

    public AddressResponse setDefaultAddress(UUID userId, UUID addressId, SetDefaultAddressRequest request) {
        Address address = findAddressByUserIdAndId(userId, addressId);

        if (Boolean.TRUE.equals(request.defaultDelivery())) {
            addressRepository.resetDefaultDelivery(userId);
            address.setDefaultDelivery(true);
        }

        if (Boolean.TRUE.equals(request.defaultBilling())) {
            addressRepository.resetDefaultBilling(userId);
            address.setDefaultBilling(true);
        }

        Address updated = addressRepository.save(address);
        return AddressMapper.toAddressResponse(updated);
    }

    private Address findAddressByUserIdAndId(UUID userId, UUID addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "ADDRESS_NOT_FOUND",
                                "Adres bulunamadı veya bu adrese erişim yetkiniz yok."
                        )
                );
    }
}
