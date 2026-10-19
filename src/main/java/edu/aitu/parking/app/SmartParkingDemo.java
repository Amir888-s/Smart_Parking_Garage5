package edu.aitu.parking.app;

import edu.aitu.parking.allocation.*;
import edu.aitu.parking.domain.*;
import edu.aitu.parking.pricing.*;
import edu.aitu.parking.sensor.SimulatedOccupancySource;
import java.util.List;

/** Console walkthrough of strategy families, auto selection, manual selection, switching. */
public final class SmartParkingDemo {
    private SmartParkingDemo() { }

    public static void main(String[] args) {
        SimulatedOccupancySource sensor = new SimulatedOccupancySource(.55);
        ParkingRequest request = new ParkingRequest(120, Membership.REGULAR, false, false);
        PricingStrategy hourly = new HourlyPricing();
        PricingStrategy capped = new DailyCapPricing();
        PricingStrategy demand = new DemandPricing(sensor);
        PricingContext pricing = new PricingContext(hourly);
        PricingSelector pricingSelector = new PricingSelector(sensor, hourly, capped, demand);

        System.out.println("=== SMART PARKING GARAGE: STRATEGY PATTERN ===");
        System.out.println("Input: 120 min, regular member, sensor = 55%");
        System.out.println("PRICING - SAME INPUT, THREE ALGORITHMS");
        for (PricingStrategy method : List.of(hourly, capped, demand)) {
            pricing.setStrategy(method);
            System.out.println(method.getClass().getSimpleName() + ": " + pricing.quote(request) + " KZT");
        }
        ParkingRequest longStay = new ParkingRequest(540, Membership.REGULAR, false, false);
        System.out.println("ADDITIONAL 9-HOUR CASE: hourly=" + hourly.calculate(longStay)
                + ", cap=" + capped.calculate(longStay)
                + ", demand=" + demand.calculate(longStay));
        System.out.println("MANUAL pricing = DemandPricing");
        pricing.setStrategy(demand);
        System.out.println("Manual result: " + pricing.quote(request) + " KZT");
        pricing.setStrategy(pricingSelector.select(request));
        System.out.println("AUTOMATIC pricing = " + pricingSelector.select(request).getClass().getSimpleName()
                + ", total = " + pricing.quote(request) + " KZT");
        sensor.setOccupancyRatio(.92);
        pricing.setStrategy(pricingSelector.select(request));
        System.out.println("SENSOR CHANGED TO 92%: " + pricingSelector.select(request).getClass().getSimpleName()
                + ", total = " + pricing.quote(request) + " KZT");

        List<ParkingSpot> spots = List.of(
                new ParkingSpot("G-01", 0, 8, false, true, true),
                new ParkingSpot("G-02", 0, 22, false, false, true),
                new ParkingSpot("G-03", 0, 30, true, false, false),
                new ParkingSpot("L1-01", 1, 12, false, true, false),
                new ParkingSpot("L1-02", 1, 18, true, false, false),
                new ParkingSpot("L1-03", 1, 50, false, false, true),
                new ParkingSpot("L2-01", 2, 55, false, false, false),
                new ParkingSpot("L2-02", 2, 75, true, false, false));
        SpotAllocationStrategy nearest = new NearestEntranceStrategy();
        SpotAllocationStrategy lowest = new LowestLevelStrategy();
        SpotAllocationStrategy balanced = new LeastCrowdedLevelStrategy();
        AllocationContext allocation = new AllocationContext(nearest);
        AllocationSelector allocationSelector = new AllocationSelector(sensor, nearest, lowest, balanced);
        System.out.println("ALLOCATION - SAME INPUT, THREE ALGORITHMS");
        for (SpotAllocationStrategy algorithm : List.of(nearest, lowest, balanced)) {
            allocation.setStrategy(algorithm);
            System.out.println(algorithm.getClass().getSimpleName() + ": "
                    + allocation.allocate(request, spots).map(ParkingSpot::id).orElse("NONE"));
        }
        allocation.setStrategy(lowest);
        System.out.println("MANUAL allocation = " + allocation.allocate(request, spots).map(ParkingSpot::id).orElse("NONE"));
        allocation.setStrategy(allocationSelector.select(request));
        System.out.println("AUTOMATIC at 92% = " + allocationSelector.select(request).getClass().getSimpleName()
                + ", spot = " + allocation.allocate(request, spots).map(ParkingSpot::id).orElse("NONE"));
        sensor.setOccupancyRatio(.55);
        allocation.setStrategy(allocationSelector.select(request));
        System.out.println("SENSOR BACK TO 55%, automatic allocation = "
                + allocationSelector.select(request).getClass().getSimpleName()
                + ", spot = " + allocation.allocate(request, spots).map(ParkingSpot::id).orElse("NONE"));
        System.out.println("RUNTIME SWAP: context instances stayed the same throughout.");
    }
}
