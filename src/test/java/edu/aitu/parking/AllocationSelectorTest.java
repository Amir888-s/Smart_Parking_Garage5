package edu.aitu.parking;

import edu.aitu.parking.allocation.*;
import edu.aitu.parking.domain.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertSame;

class AllocationSelectorTest {
    private final SpotAllocationStrategy nearest = new NearestEntranceStrategy();
    private final SpotAllocationStrategy lowest = new LowestLevelStrategy();
    private final SpotAllocationStrategy leastCrowded = new LeastCrowdedLevelStrategy();

    private AllocationSelector selector(double occupancy) {
        return new AllocationSelector(new FakeOccupancySource(occupancy), nearest, lowest, leastCrowded);
    }
    private ParkingRequest request(Membership tier, boolean accessible) {
        return new ParkingRequest(60, tier, false, accessible);
    }

    @Test void regularDriverUsesLowestLevelOnQuietDay() {
        assertSame(lowest, selector(.30).select(request(Membership.REGULAR, false)));
    }
    @Test void vipUsesNearestOnQuietDay() {
        assertSame(nearest, selector(.30).select(request(Membership.VIP, false)));
    }
    @Test void congestionUsesLeastCrowdedLevel() {
        assertSame(leastCrowded, selector(.80).select(request(Membership.REGULAR, false)));
    }
    @Test void accessibleSpaceRequirementTakesPriorityOverCongestion() {
        assertSame(nearest, selector(.99).select(request(Membership.REGULAR, true)));
    }
    @Test void congestionTakesPriorityOverVip() {
        assertSame(leastCrowded, selector(.90).select(request(Membership.VIP, false)));
    }
}
