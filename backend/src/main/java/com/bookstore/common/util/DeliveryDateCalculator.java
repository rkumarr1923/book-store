package com.bookstore.common.util;

import com.bookstore.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Calculates the estimated delivery date for an order.
 * Phase 1: simple calendar offset from the current date.
 * Phase 2: integrate with a logistics API for dynamic estimates.
 */
@Component
@RequiredArgsConstructor
public class DeliveryDateCalculator {

    private final AppProperties appProperties;

    /**
     * Returns the estimated delivery date based on today plus the configured
     * offset days from {@code app.delivery.offset-days}.
     *
     * @return estimated delivery date
     */
    public LocalDate calculate() {
        return LocalDate.now().plusDays(appProperties.getDelivery().getOffsetDays());
    }

    /**
     * Returns the estimated delivery date from a given base date.
     *
     * @param from the base date (e.g. order placement date)
     * @return estimated delivery date
     */
    public LocalDate calculateFrom(LocalDate from) {
        return from.plusDays(appProperties.getDelivery().getOffsetDays());
    }
}
