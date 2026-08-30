package com.carmats.user.entity;

import com.carmats.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "addresses")
public class Address extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

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

    @Column(name = "address_line", nullable = false, length = 500)
    private String addressLine;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "company_name", length = 150)
    private String companyName;

    @Column(name = "tax_number", length = 50)
    private String taxNumber;

    @Column(name = "tax_office", length = 100)
    private String taxOffice;

    @Column(name = "is_default_delivery", nullable = false)
    private boolean defaultDelivery = false;

    @Column(name = "is_default_billing", nullable = false)
    private boolean defaultBilling = false;

    protected Address() {
    }

    public Address(
            User user,
            String title,
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
            String taxOffice,
            boolean defaultDelivery,
            boolean defaultBilling
    ) {
        this.user = user;
        this.title = title;
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
        this.defaultDelivery = defaultDelivery;
        this.defaultBilling = defaultBilling;
    }

    public void update(
            String title,
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
            String taxOffice,
            boolean defaultDelivery,
            boolean defaultBilling
    ) {
        this.title = title;
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
        this.defaultDelivery = defaultDelivery;
        this.defaultBilling = defaultBilling;
    }

    public User getUser() {
        return user;
    }

    public String getTitle() {
        return title;
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

    public boolean isDefaultDelivery() {
        return defaultDelivery;
    }

    public void setDefaultDelivery(boolean defaultDelivery) {
        this.defaultDelivery = defaultDelivery;
    }

    public boolean isDefaultBilling() {
        return defaultBilling;
    }

    public void setDefaultBilling(boolean defaultBilling) {
        this.defaultBilling = defaultBilling;
    }
}
