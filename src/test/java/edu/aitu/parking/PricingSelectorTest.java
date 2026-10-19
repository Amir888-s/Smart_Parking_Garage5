package edu.aitu.parking;

import edu.aitu.parking.domain.Membership;
import edu.aitu.parking.domain.ParkingRequest;
import edu.aitu.parking.pricing.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertSame;

class PricingSelectorTest {
    private final PricingStrategy hourly = new HourlyPricing();
    private final PricingStrategy capped = new DailyCapPricing();
    private final PricingStrategy demand = new DemandPricing(new FakeOccupancySource(.5));

    private PricingSelector selector(double occupancy) {
        return new PricingSelector(new FakeOccupancySource(occupancy), hourly, capped, demand);
    }
    private ParkingRequest request(long minutes, Membership tier) {
        return new ParkingRequest(minutes, tier, false, false);
    }

    @Test void shortRegularStaySelectsHourly() {
        assertSame(hourly, selector(.40).select(request(60, Membership.REGULAR)));
    }
    @Test void longRegularStaySelectsDailyCap() {
        assertSame(capped, selector(.40).select(request(360, Membership.REGULAR)));
    }
    @Test void crowdedGarageSelectsDemand() {
        assertSame(demand, selector(.85).select(request(60, Membership.REGULAR)));
    }
    @Test void longVipStayKeepsCapEvenDuringCrowding() {
        assertSame(capped, selector(.95).select(request(180, Membership.VIP)));
    }
    @Test void shortVipStayCanUseDemandWhenCrowded() {
        assertSame(demand, selector(.90).select(request(120, Membership.VIP)));
    }
}
