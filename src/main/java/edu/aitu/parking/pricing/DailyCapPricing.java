package edu.aitu.parking.pricing;

import edu.aitu.parking.domain.ParkingRequest;
import java.math.BigDecimal;

/** Split a stay into 24-hour blocks, each limited to KZT 3,000. */
public final class DailyCapPricing implements PricingStrategy {
    private static final long DAY_MINUTES = 1440;
    @Override public BigDecimal calculate(ParkingRequest request) {
        long remaining = request.stayMinutes();
        long total = 0;
        while (remaining > 0) {
            long block = Math.min(remaining, DAY_MINUTES);
            long hours = Math.floorDiv(block - 1, 60) + 1;
            total += Math.min(hours * 500L, 3000L);
            remaining -= block;
        }
        return BigDecimal.valueOf(total);
    }
}
