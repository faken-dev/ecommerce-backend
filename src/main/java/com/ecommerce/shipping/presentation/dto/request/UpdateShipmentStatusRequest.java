package com.ecommerce.shipping.presentation.dto.request;

import com.ecommerce.shipping.domain.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateShipmentStatusRequest {
    @NotNull
    private ShipmentStatus status;
    private String note; // Optional note for the status change
}
