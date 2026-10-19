package edu.aitu.parking.allocation;

import edu.aitu.parking.domain.ParkingRequest;
import edu.aitu.parking.domain.ParkingSpot;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Choose the smallest floor number, using walking distance as a tie breaker. */
public final class LowestLevelStrategy implements SpotAllocationStrategy {
    @Override public Optional<ParkingSpot> choose(ParkingRequest request, List<ParkingSpot> spaces) {
        return spaces.stream().filter(s -> s.isSuitableFor(request))
                .min(Comparator.comparingInt(ParkingSpot::level)
                        .thenComparingInt(ParkingSpot::distanceMeters).thenComparing(ParkingSpot::id));
    }
}
