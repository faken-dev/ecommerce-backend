package com.ecommerce.admin.application.usecase;

import com.ecommerce.order.infrastructure.persistence.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SalesAnalyticsUseCase {

    private final OrderJpaRepository orderJpaRepository;

    public Map<LocalDate, BigDecimal> getDailySales(int days) {
        Map<LocalDate, BigDecimal> results = new LinkedHashMap<>();
        LocalDate today = LocalDate.now(ZoneId.of("UTC"));

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Instant startOfDay = date.atStartOfDay(ZoneId.of("UTC")).toInstant();
            Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant();
            
            BigDecimal revenue = orderJpaRepository.sumTotalAmountBetween(startOfDay, endOfDay);
            results.put(date, revenue != null ? revenue : BigDecimal.ZERO);
        }
        return results;
    }
}
