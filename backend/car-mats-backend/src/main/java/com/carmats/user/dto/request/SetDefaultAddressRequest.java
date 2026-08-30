package com.carmats.user.dto.request;

public record SetDefaultAddressRequest(
        Boolean defaultDelivery,
        Boolean defaultBilling
) {
}
