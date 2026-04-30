package com.ecommerce.shipping.domain;

public interface CarrierProvider {
    CarrierService getCarrier(String name);
}
