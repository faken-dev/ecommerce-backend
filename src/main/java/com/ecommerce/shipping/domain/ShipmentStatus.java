package com.ecommerce.shipping.domain;

/**
 * Lifecycle of a physical shipment.
 * No garbage "STEP_1", "STEP_2" names. Use real-world logistics terminology.
 */
public enum ShipmentStatus {
    PENDING,        // Shipment created in system, not yet handed to carrier
    PROCESSING,     // Label printed, package being prepared
    PICKED_UP,      // Carrier has the package
    IN_TRANSIT,     // On the way to destination
    OUT_FOR_DELIVERY, // Final mile delivery
    DELIVERED,      // Successfully delivered
    FAILED,         // Attempted but failed
    RETURNED,       // Sent back to origin
    CANCELLED       // Cancelled before pickup
}
