package com.ecommerce.user.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Presentation-layer request to update an address.
 * Address ID is taken from the path, not this request.
 */
public record UpdateAddressRequest(
        @NotBlank(message = "Recipient name is required")
        @Size(max = 255, message = "Recipient name must not exceed 255 characters")
        String recipientName,

        @NotBlank(message = "Recipient phone is required")
        @Size(max = 20, message = "Recipient phone must not exceed 20 characters")
        String recipientPhone,

        @NotBlank(message = "Address line is required")
        @Size(max = 500, message = "Address line must not exceed 500 characters")
        String addressLine,

        @NotBlank(message = "Ward is required")
        @Size(max = 255, message = "Ward must not exceed 255 characters")
        String ward,

        @NotBlank(message = "District is required")
        @Size(max = 255, message = "District must not exceed 255 characters")
        String district,

        @NotBlank(message = "Province is required")
        @Size(max = 255, message = "Province must not exceed 255 characters")
        String province
) {}
