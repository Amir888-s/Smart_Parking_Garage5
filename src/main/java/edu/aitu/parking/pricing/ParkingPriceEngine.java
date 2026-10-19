package edu.aitu.parking.pricing;

import edu.aitu.parking.domain.ParkingRequest;
import edu.aitu.parking.sensor.OccupancySource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** BEFORE refactoring: pricing algorithms are mixed into one class. */
public final class ParkingPriceEngine {
    private final OccupancySource occupancySource;

    public ParkingPriceEngine(OccupancySource occupancySource) {
        this.occupancySource = Objects.requireNonNull(occupancySource);
    }

    public BigDecimal quote(ParkingRequest request, PricingPlan plan) {
        Objects.requireNonNull(request); Objects.requireNonNull(plan);
        long minutes = request.stayMinutes();
        if (plan == PricingPlan.HOURLY) {
            return BigDecimal.valueOf(((minutes + 59) / 60) * 500L);
        } else if (plan == PricingPlan.DAILY_CAP) {
            long price = 0;
            while (minutes > 0) {
                long block = Math.min(minutes, 1440);
                price += Math.min(((block + 59) / 60) * 500L, 3000L);
                minutes -= block;
            }
            return BigDecimal.valueOf(price);
        } else {
            BigDecimal base = BigDecimal.valueOf(600L + ((minutes + 29) / 30) * 250L);
            double ratio = occupancySource.currentOccupancyRatio();
            BigDecimal factor = ratio >= .85 ? new BigDecimal("1.50")
                    : ratio >= .70 ? new BigDecimal("1.20") : BigDecimal.ONE;
            return base.multiply(factor).setScale(0, RoundingMode.HALF_UP);
        }
    }
}
