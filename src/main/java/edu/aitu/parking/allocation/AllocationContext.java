package edu.aitu.parking.allocation;

import edu.aitu.parking.domain.ParkingRequest;
import edu.aitu.parking.domain.ParkingSpot;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Context #2: delegates the space choice to the active algorithm. */
public final class AllocationContext {
    private SpotAllocationStrategy strategy;

    public AllocationContext(SpotAllocationStrategy strategy) { setStrategy(strategy); }

    public void setStrategy(SpotAllocationStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "allocation strategy");
    }

    public Optional<ParkingSpot> allocate(ParkingRequest request, List<ParkingSpot> spaces) {
        return strategy.choose(request, spaces);
    }
}
