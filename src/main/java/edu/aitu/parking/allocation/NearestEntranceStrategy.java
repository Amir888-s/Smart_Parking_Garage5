package edu.aitu.parking.allocation;

import edu.aitu.parking.domain.ParkingRequest;
import edu.aitu.parking.domain.ParkingSpot;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Find the suitable free space with the shortest walk. */
public final class NearestEntranceStrategy implements SpotAllocationStrategy {
    @Override public Optional<ParkingSpot> choose(ParkingRequest request, List<ParkingSpot> spaces) {
        return spaces.stream().filter(s -> s.isSuitableFor(request))
                .min(Comparator.comparingInt(ParkingSpot::distanceMeters).thenComparing(ParkingSpot::id));
    }
}
