package edu.aitu.parking;

import edu.aitu.parking.domain.Membership;
import edu.aitu.parking.domain.ParkingRequest;
import edu.aitu.parking.pricing.ParkingPriceEngine;
import edu.aitu.parking.pricing.PricingPlan;
import edu.aitu.parking.sensor.SimulatedOccupancySource;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** This exact file exists unchanged in both Git refactoring commits. */
class PricingBehaviorRegressionTest {
    @Test void hourlyRoundsUpToFullHours() {
        ParkingPriceEngine engine = new ParkingPriceEngine(new SimulatedOccupancySource(.50));
        assertEquals(new BigDecimal("1000"),
                engine.quote(new ParkingRequest(61, Membership.REGULAR, false, false), PricingPlan.HOURLY));
    }

    @Test void cappedPriceStopsAtDailyMaximum() {
        ParkingPriceEngine engine = new ParkingPriceEngine(new SimulatedOccupancySource(.50));
        assertEquals(new BigDecimal("3000"),
                engine.quote(new ParkingRequest(540, Membership.REGULAR, false, false), PricingPlan.DAILY_CAP));
    }

    @Test void demandPriceUsesOccupancySurcharge() {
        ParkingPriceEngine engine = new ParkingPriceEngine(new SimulatedOccupancySource(.90));
        assertEquals(new BigDecimal("1650"),
                engine.quote(new ParkingRequest(60, Membership.REGULAR, false, false), PricingPlan.DEMAND));
    }
}
