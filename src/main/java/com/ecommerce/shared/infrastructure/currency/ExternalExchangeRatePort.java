package com.ecommerce.shared.infrastructure.currency;

import com.ecommerce.shared.application.port.ExchangeRatePort;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of ExchangeRatePort that fetches real-time rates from an external API.
 * Uses a simple in-memory cache to avoid excessive API calls.
 */

@Service
@RequiredArgsConstructor
public class ExternalExchangeRatePort implements ExchangeRatePort {
    private static final Logger log = LoggerFactory.getLogger(ExternalExchangeRatePort.class);

    private final RestTemplate restTemplate;
    
    // Simple cache: Map<BaseCurrency, RateData>
    private final Map<String, RateData> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 3600_000; // 1 hour

    private static final String API_URL = "https://open.er-api.com/v6/latest/";

    @Override
    public BigDecimal getRate(String sourceCurrency, String targetCurrency) {
        if (sourceCurrency.equals(targetCurrency)) return BigDecimal.ONE;

        RateData data = cache.get(sourceCurrency);
        if (data == null || System.currentTimeMillis() > data.expiry) {
            data = fetchRates(sourceCurrency);
            if (data != null) {
                cache.put(sourceCurrency, data);
            }
        }

        if (data != null && data.rates.containsKey(targetCurrency)) {
            BigDecimal rate = data.rates.get(targetCurrency);
            log.info("Exchange rate found: 1 {} = {} {}", sourceCurrency, rate, targetCurrency);
            return rate;
        }

        // Realistic fallback if API fails (approx 1 USD = 25000 VND)
        BigDecimal fallbackRate = sourceCurrency.equals("VND") && targetCurrency.equals("USD") 
                ? new BigDecimal("0.00004") 
                : BigDecimal.ONE;

        log.warn("Exchange rate not found for {} -> {}. Using fallback: {}", sourceCurrency, targetCurrency, fallbackRate);
        return fallbackRate;
    }

    private RateData fetchRates(String base) {
        try {
            log.info("Fetching exchange rates for base: {}", base);
            JsonNode response = restTemplate.getForObject(API_URL + base, JsonNode.class);
            
            if (response != null && "success".equals(response.get("result").asText())) {
                JsonNode ratesNode = response.get("rates");
                Map<String, BigDecimal> rates = new ConcurrentHashMap<>();
                ratesNode.properties().forEach(entry -> {
                    rates.put(entry.getKey(), new BigDecimal(entry.getValue().asText()));
                });
                
                return new RateData(rates, System.currentTimeMillis() + CACHE_TTL_MS);
            }
        } catch (Exception e) {
            log.error("Failed to fetch exchange rates for {}", base, e);
        }
        return null;
    }

    private record RateData(Map<String, BigDecimal> rates, long expiry) {}
}
