package com.ecommerce.shipping.domain;

import java.math.BigDecimal;

/**
 * Interface for external shipping carriers (Port).
 */
public interface CarrierService {
    /**
     * @return Unique tracking number from the carrier.
     */
    String createOrder(Shipment shipment);
    
    /**
     * @return Estimated shipping fee from carrier.
     */
    BigDecimal calculateFee(Shipment shipment);
    
    /**
     * Cancels the shipment on the carrier side.
     */
    void cancelOrder(String trackingNumber);

    /**
     * Returns the name of the carrier (e.g., GHN, SPX).
     */
    String getCarrierName();
}
