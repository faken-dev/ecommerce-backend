package com.ecommerce.shipping.infrastructure.carrier;

import com.ecommerce.shipping.domain.CarrierService;
import com.ecommerce.shipping.domain.Shipment;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Adapter for Giao Hang Nhanh (GHN) API.
 * External API calls are messy, keep the failure handling robust.
 */
@Service

public class GhnCarrierAdapter implements CarrierService {
    private static final Logger log = LoggerFactory.getLogger(GhnCarrierAdapter.class);

    @Override
    public String createOrder(Shipment shipment) {
        log.info("Creating GHN order for shipment: {}", shipment.getId());
        // TODO: Real API call to GHN (https://api.ghn.vn)
        // For now, return a mock tracking number
        return "GHN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public BigDecimal calculateFee(Shipment shipment) {
        // GHN fee calculation logic based on weight and distance
        return BigDecimal.valueOf(35000.0); 
    }

    @Override
    public void cancelOrder(String trackingNumber) {
        log.info("Cancelling GHN order: {}", trackingNumber);
    }

    @Override
    public String getCarrierName() {
        return "GHN";
    }
}
