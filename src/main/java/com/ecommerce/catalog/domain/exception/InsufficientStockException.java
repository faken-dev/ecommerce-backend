package com.ecommerce.catalog.domain.exception;

import com.ecommerce.shared.domain.exception.DomainException;

public class InsufficientStockException extends DomainException {
    public InsufficientStockException(String productName, int currentStock, int requestedChange) {
        super(String.format("Insufficient stock for product '%s'. Current: %d, Requested: %d", 
                productName, currentStock, Math.abs(requestedChange)), 400);
    }
}
