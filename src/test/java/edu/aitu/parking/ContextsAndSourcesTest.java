package edu.aitu.parking;

import edu.aitu.parking.allocation.*;
import edu.aitu.parking.domain.*;
import edu.aitu.parking.pricing.*;
import edu.aitu.parking.sensor.SimulatedOccupancySource;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ContextsAndSourcesTest {
    private final ParkingRequest request = new ParkingRequest(480, Membership.REGULAR, false, false);

    @Test void pricingContextChangesAlgorithmAtRuntime() {
        PricingContext context = new PricingContext(new HourlyPricing());
        assertEquals(new BigDecimal("4000"), context.quote(request));
        context.setStrategy(new DailyCapPricing());
        assertEquals(new BigDecimal("3000"), context.quote(request));
    }

    @Test void allocationContextChangesAlgorithmAtRuntime() {
        List<ParkingSpot> spaces = List.of(
                new ParkingSpot("G", 0, 100, false, false, false),
                new ParkingSpot("UP", 1, 10, false, false, false));
        AllocationContext context = new AllocationContext(new LowestLevelStrategy());
        assertEquals("G", context.allocate(request, spaces).orElseThrow().id());
        context.setStrategy(new NearestEntranceStrategy());
        assertEquals("UP", context.allocate(request, spaces).orElseThrow().id());
    }

    @Test void swappedOccupancySourceChangesDemandPrice() {
        PricingStrategy quiet = new DemandPricing(new FakeOccupancySource(.10));
        PricingStrategy busy = new DemandPricing(new FakeOccupancySource(.95));
        PricingContext context = new PricingContext(quiet);
        BigDecimal normal = context.quote(request);
        context.setStrategy(busy);
        assertTrue(context.quote(request).compareTo(normal) > 0);
    }

    @Test void simulatedSensorCanChangeAtRuntime() {
        SimulatedOccupancySource sensor = new SimulatedOccupancySource(.50);
        assertEquals(.50, sensor.currentOccupancyRatio());
        sensor.setOccupancyRatio(.91);
        assertEquals(.91, sensor.currentOccupancyRatio());
        assertThrows(IllegalArgumentException.class, () -> sensor.setOccupancyRatio(1.2));
    }

    @Test void nullStrategiesAreRejectedByContexts() {
        assertThrows(NullPointerException.class, () -> new PricingContext(null));
        assertThrows(NullPointerException.class, () -> new AllocationContext(null));
    }

    @Test void requestMustHavePositiveDuration() {
        assertThrows(IllegalArgumentException.class,
                () -> new ParkingRequest(0, Membership.REGULAR, false, false));
    }
}
