package edu.aitu.parking.pricing;

import edu.aitu.parking.domain.ParkingRequest;
import edu.aitu.parking.sensor.OccupancySource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/** Stable public pricing entry point retained across the refactoring commits. */
public final class ParkingPriceEngine {
    private final Map<PricingPlan, PricingStrategy> strategies;
    private final PricingContext context;

    public ParkingPriceEngine(OccupancySource occupancySource) {
        Objects.requireNonNull(occupancySource, "occupancy source");
        this.strategies = Map.of(
                PricingPlan.HOURLY, new HourlyPricing(),
                PricingPlan.DAILY_CAP, new DailyCapPricing(),
                PricingPlan.DEMAND, new DemandPricing(occupancySource));
        this.context = new PricingContext(strategies.get(PricingPlan.HOURLY));
    }

    public BigDecimal quote(ParkingRequest request, PricingPlan plan) {
        context.setStrategy(Objects.requireNonNull(strategies.get(Objects.requireNonNull(plan)), "pricing plan"));
        return context.quote(request);
    }
}
