package edu.aitu.parking.allocation;

import edu.aitu.parking.domain.ParkingRequest;
import edu.aitu.parking.domain.ParkingSpot;
import java.util.List;
import java.util.Optional;

/** Strategy family #2: interchangeable parking-space selection algorithms. */
public interface SpotAllocationStrategy {
    Optional<ParkingSpot> choose(ParkingRequest request, List<ParkingSpot> spaces);
}
