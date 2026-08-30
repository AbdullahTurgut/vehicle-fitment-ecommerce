package com.carmats.user.mapper;

import com.carmats.user.dto.response.AddressResponse;
import com.carmats.user.entity.Address;

public final class AddressMapper {

    private AddressMapper() {
    }

    public static AddressResponse toAddressResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getUser().getId(),
                address.getTitle(),
                address.getFirstName(),
                address.getLastName(),
                address.getPhoneNumber(),
                address.getCity(),
                address.getDistrict(),
                address.getNeighborhood(),
                address.getAddressLine(),
                address.getPostalCode(),
                address.getCompanyName(),
                address.getTaxNumber(),
                address.getTaxOffice(),
                address.isDefaultDelivery(),
                address.isDefaultBilling(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }
}
