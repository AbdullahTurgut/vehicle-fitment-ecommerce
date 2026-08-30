package com.carmats.order.entity;

import com.carmats.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "order_addresses")
public class OrderAddress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 20)
    private OrderAddressType addressType;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone_number", nullable = false, length = 30)
    private String phoneNumber;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String district;

    @Column(length = 150)
    private String neighborhood;

    @Column(name = "address_line", nullable = false, columnDefinition = "TEXT")
    private String addressLine;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "tax_number", length = 50)
    private String taxNumber;

    @Column(name = "tax_office", length = 100)
    private String taxOffice;

    protected OrderAddress() {
    }

    public OrderAddress(
            Order order,
            OrderAddressType addressType,
            String firstName,
            String lastName,
            String phoneNumber,
            String city,
            String district,
            String neighborhood,
            String addressLine,
            String postalCode,
            String companyName,
            String taxNumber,
            String taxOffice
    ) {
        this.order = order;
        this.addressType = addressType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.city = city;
        this.district = district;
        this.neighborhood = neighborhood;
        this.addressLine = addressLine;
        this.postalCode = postalCode;
        this.companyName = companyName;
        this.taxNumber = taxNumber;
        this.taxOffice = taxOffice;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public OrderAddressType getAddressType() {
        return addressType;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public String getTaxOffice() {
        return taxOffice;
    }
}
