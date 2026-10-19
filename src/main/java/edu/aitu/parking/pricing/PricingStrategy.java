package edu.aitu.parking.pricing;

import edu.aitu.parking.domain.ParkingRequest;
import java.math.BigDecimal;

/** Strategy family #1: interchangeable parking-price calculations. */
public interface PricingStrategy {
    BigDecimal calculate(ParkingRequest request);
}
