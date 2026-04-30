package com.ecommerce.shipping.infrastructure.carrier;

import com.ecommerce.shipping.domain.CarrierProvider;
import com.ecommerce.shipping.domain.CarrierService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CarrierFactory implements CarrierProvider {
    private final Map<String, CarrierService> carriers;

    public CarrierFactory(List<CarrierService> carrierList) {
        this.carriers = carrierList.stream()
                .collect(Collectors.toMap(
                    s -> s.getCarrierName().toUpperCase(), 
                    Function.identity()
                ));
    }

    public CarrierService getCarrier(String name) {
        CarrierService carrier = carriers.get(name.toUpperCase());
        if (carrier == null) {
            throw new IllegalArgumentException("Unsupported carrier: " + name);
        }
        return carrier;
    }
}
