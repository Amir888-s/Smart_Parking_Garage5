package edu.aitu.parking.allocation;

import edu.aitu.parking.domain.Membership;
import edu.aitu.parking.domain.ParkingRequest;
import edu.aitu.parking.sensor.OccupancySource;
import java.util.Objects;

/** Automatically selects a parking-space allocation strategy. */
public final class AllocationSelector {
    private final OccupancySource occupancySource;
    private final SpotAllocationStrategy nearest;
    private final SpotAllocationStrategy lowest;
    private final SpotAllocationStrategy leastCrowded;

    public AllocationSelector(OccupancySource occupancySource, SpotAllocationStrategy nearest,
                              SpotAllocationStrategy lowest, SpotAllocationStrategy leastCrowded) {
        this.occupancySource = Objects.requireNonNull(occupancySource);
        this.nearest = Objects.requireNonNull(nearest);
        this.lowest = Objects.requireNonNull(lowest);
        this.leastCrowded = Objects.requireNonNull(leastCrowded);
    }

    public SpotAllocationStrategy select(ParkingRequest request) {
        if (request.accessibleSpace()) return nearest;
        if (occupancySource.currentOccupancyRatio() >= .80) return leastCrowded;
        if (request.membership() == Membership.VIP) return nearest;
        return lowest;
    }
}
