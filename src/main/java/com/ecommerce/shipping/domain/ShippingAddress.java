package com.ecommerce.shipping.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Value Object for shipping address.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShippingAddress {
    private String recipientName;
    private String phone;
    private String street;
    private String district;
    private String city;
    private String province;
    private String country;
    private String postalCode;

    public String getFormattedAddress() {
        return String.format("%s, %s, %s, %s, %s", street, district, city, province, country);
    }
}
