package edu.aitu.parking.pricing;

import edu.aitu.parking.domain.ParkingRequest;
import edu.aitu.parking.sensor.OccupancySource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Entry fee plus half-hour slots, adjusted using live occupancy data. */
public final class DemandPricing implements PricingStrategy {
    private final OccupancySource occupancySource;

    public DemandPricing(OccupancySource occupancySource) {
        this.occupancySource = Objects.requireNonNull(occupancySource);
    }

    @Override public BigDecimal calculate(ParkingRequest request) {
        long slots = Math.floorDiv(request.stayMinutes() - 1, 30) + 1;
        BigDecimal base = BigDecimal.valueOf(slots).multiply(BigDecimal.valueOf(250))
                .add(BigDecimal.valueOf(600));
        double occupancy = occupancySource.currentOccupancyRatio();
        BigDecimal multiplier = occupancy >= .85 ? new BigDecimal("1.50")
                : occupancy >= .70 ? new BigDecimal("1.20") : BigDecimal.ONE;
        return base.multiply(multiplier).setScale(0, RoundingMode.HALF_UP);
    }
}
