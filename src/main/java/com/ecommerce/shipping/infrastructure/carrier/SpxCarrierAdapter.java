package com.ecommerce.shipping.infrastructure.carrier;

import com.ecommerce.shipping.domain.CarrierService;
import com.ecommerce.shipping.domain.Shipment;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Adapter for SPX (Shopee Xpress) API.
 */
@Service

public class SpxCarrierAdapter implements CarrierService {
    private static final Logger log = LoggerFactory.getLogger(SpxCarrierAdapter.class);

    @Override
    public String createOrder(Shipment shipment) {
        log.info("Creating SPX order for shipment: {}", shipment.getId());
        // TODO: Real API call to SPX
        return "SPX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public BigDecimal calculateFee(Shipment shipment) {
        return BigDecimal.valueOf(40000.0);
    }

    @Override
    public void cancelOrder(String trackingNumber) {
        log.info("Cancelling SPX order: {}", trackingNumber);
    }

    @Override
    public String getCarrierName() {
        return "SPX";
    }
}
