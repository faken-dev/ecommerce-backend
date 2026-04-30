package com.ecommerce.shipping.infrastructure.persistence.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddressEmbeddable {
    private String recipientName;
    private String phone;
    private String street;
    private String district;
    private String city;
    private String province;
    private String country;
    private String postalCode;
}
