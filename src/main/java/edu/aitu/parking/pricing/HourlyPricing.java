package edu.aitu.parking.pricing;

import edu.aitu.parking.domain.ParkingRequest;
import java.math.BigDecimal;

/** Charge KZT 500 per started hour. */
public final class HourlyPricing implements PricingStrategy {
    @Override public BigDecimal calculate(ParkingRequest request) {
        long billedHours = Math.floorDiv(request.stayMinutes() - 1, 60) + 1;
        return BigDecimal.valueOf(billedHours).multiply(BigDecimal.valueOf(500));
    }
}
