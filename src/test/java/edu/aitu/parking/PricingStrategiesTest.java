package edu.aitu.parking;

import edu.aitu.parking.domain.Membership;
import edu.aitu.parking.domain.ParkingRequest;
import edu.aitu.parking.pricing.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.math.BigDecimal;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class PricingStrategiesTest {
    private static ParkingRequest request(long minutes) {
        return new ParkingRequest(minutes, Membership.REGULAR, false, false);
    }

    @Test void hourlyChargesOneWholeHourForOneMinute() {
        assertEquals(new BigDecimal("500"), new HourlyPricing().calculate(request(1)));
    }

    @Test void cappedPricingResetsTheCapForSecondDay() {
        assertEquals(new BigDecimal("4000"), new DailyCapPricing().calculate(request(26 * 60)));
    }

    @Test void demandPricingUsesFakeSensorAtHighOccupancy() {
        assertEquals(new BigDecimal("1650"),
                new DemandPricing(new FakeOccupancySource(.90)).calculate(request(60)));
    }

    @Test void demandPricingHasNoSurchargeWhenQuiet() {
        assertEquals(new BigDecimal("1100"),
                new DemandPricing(new FakeOccupancySource(.20)).calculate(request(60)));
    }

    @Test void demandPricingUsesMiddleTierAtSeventyPercent() {
        assertEquals(new BigDecimal("1320"),
                new DemandPricing(new FakeOccupancySource(.70)).calculate(request(60)));
    }

    @ParameterizedTest(name = "{0} never returns a negative price")
    @MethodSource("allStrategies")
    void everyPricingStrategyIsNonnegative(String name, PricingStrategy pricing) {
        for (long minutes : new long[] {1, 29, 60, 61, 360, 1500}) {
            assertTrue(pricing.calculate(request(minutes)).signum() >= 0, name);
        }
    }

    private static Stream<Arguments> allStrategies() {
        return Stream.of(
                Arguments.of("Hourly", new HourlyPricing()),
                Arguments.of("Daily cap", new DailyCapPricing()),
                Arguments.of("Demand", new DemandPricing(new FakeOccupancySource(.94))));
    }
}
