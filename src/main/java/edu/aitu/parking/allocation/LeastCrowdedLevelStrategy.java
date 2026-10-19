package edu.aitu.parking.allocation;

import edu.aitu.parking.domain.ParkingRequest;
import edu.aitu.parking.domain.ParkingSpot;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Route arrivals toward floors with the highest fraction of free spaces. */
public final class LeastCrowdedLevelStrategy implements SpotAllocationStrategy {
    @Override public Optional<ParkingSpot> choose(ParkingRequest request, List<ParkingSpot> spaces) {
        Map<Integer, Long> totalByLevel = spaces.stream()
                .collect(Collectors.groupingBy(ParkingSpot::level, Collectors.counting()));
        Map<Integer, Long> freeByLevel = spaces.stream().filter(s -> !s.occupied())
                .collect(Collectors.groupingBy(ParkingSpot::level, Collectors.counting()));

        return spaces.stream().filter(s -> s.isSuitableFor(request))
                .min(Comparator.<ParkingSpot>comparingDouble(s ->
                        freeByLevel.getOrDefault(s.level(), 0L) / (double) totalByLevel.get(s.level()))
                        .reversed().thenComparingInt(ParkingSpot::level)
                        .thenComparingInt(ParkingSpot::distanceMeters)
                        .thenComparing(ParkingSpot::id));
    }
}
