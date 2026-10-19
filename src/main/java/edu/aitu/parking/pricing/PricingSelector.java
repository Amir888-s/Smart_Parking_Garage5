package edu.aitu.parking.pricing;

import edu.aitu.parking.domain.Membership;
import edu.aitu.parking.domain.ParkingRequest;
import edu.aitu.parking.sensor.OccupancySource;
import java.util.Objects;

/** The ONLY pricing component deciding which strategy suits current conditions. */
public final class PricingSelector {
    private final OccupancySource occupancySource;
    private final PricingStrategy hourly;
    private final PricingStrategy capped;
    private final PricingStrategy demand;

    public PricingSelector(OccupancySource occupancySource, PricingStrategy hourly,
                           PricingStrategy capped, PricingStrategy demand) {
        this.occupancySource = Objects.requireNonNull(occupancySource);
        this.hourly = Objects.requireNonNull(hourly);
        this.capped = Objects.requireNonNull(capped);
        this.demand = Objects.requireNonNull(demand);
    }

    public PricingStrategy select(ParkingRequest request) {
        // Order is intentional: loyal long-stay customers retain their cap.
        if (request.membership() == Membership.VIP && request.stayMinutes() >= 180) return capped;
        if (occupancySource.currentOccupancyRatio() >= .85) return demand;
        if (request.stayMinutes() >= 360) return capped;
        return hourly;
    }
}
