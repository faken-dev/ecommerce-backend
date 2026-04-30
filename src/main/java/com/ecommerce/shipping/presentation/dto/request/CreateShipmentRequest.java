package com.ecommerce.shipping.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateShipmentRequest {
    @NotNull
    private UUID orderId;
    
    @NotBlank
    private String recipientName;
    
    @NotBlank
    private String phone;
    
    @NotBlank
    private String street;
    
    @NotBlank
    private String district;
    
    @NotBlank
    private String city;
    
    @NotBlank
    private String province;
    
    private String country = "Vietnam";
    
    private Double weight;
    private Double shippingFee;
}
