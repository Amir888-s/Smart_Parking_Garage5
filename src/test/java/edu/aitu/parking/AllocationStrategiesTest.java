package edu.aitu.parking;

import edu.aitu.parking.allocation.*;
import edu.aitu.parking.domain.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AllocationStrategiesTest {
    private final ParkingRequest normal = new ParkingRequest(60, Membership.REGULAR, false, false);
    private final List<ParkingSpot> spots = List.of(
            new ParkingSpot("A", 0, 100, false, false, false),
            new ParkingSpot("B", 1, 10, false, true, false),
            new ParkingSpot("C", 0, 20, false, false, true),
            new ParkingSpot("D", 1, 30, true, false, false),
            new ParkingSpot("E", 1, 40, false, false, false));

    @Test void nearestStrategyPrioritizesWalkingDistance() {
        assertEquals("B", new NearestEntranceStrategy().choose(normal, spots).orElseThrow().id());
    }
    @Test void lowestLevelStrategyPrioritizesFloor() {
        assertEquals("A", new LowestLevelStrategy().choose(normal, spots).orElseThrow().id());
    }
    @Test void leastCrowdedStrategyPrioritizesFreeFloorPercentage() {
        assertEquals("B", new LeastCrowdedLevelStrategy().choose(normal, spots).orElseThrow().id());
    }
    @Test void allStrategiesRejectOccupiedOrUnsuitableSpaces() {
        ParkingRequest charging = new ParkingRequest(60, Membership.REGULAR, true, false);
        for (SpotAllocationStrategy strategy : List.of(new NearestEntranceStrategy(),
                new LowestLevelStrategy(), new LeastCrowdedLevelStrategy())) {
            assertEquals("D", strategy.choose(charging, spots).orElseThrow().id());
        }
    }
    @Test void allStrategiesReturnEmptyWhenNoSpaceFits() {
        ParkingRequest chargerAndAccessible = new ParkingRequest(60, Membership.REGULAR, true, true);
        for (SpotAllocationStrategy strategy : List.of(new NearestEntranceStrategy(),
                new LowestLevelStrategy(), new LeastCrowdedLevelStrategy())) {
            assertTrue(strategy.choose(chargerAndAccessible, spots).isEmpty());
        }
    }
}
