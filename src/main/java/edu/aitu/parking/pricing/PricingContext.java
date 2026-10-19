package edu.aitu.parking.pricing;

import edu.aitu.parking.domain.ParkingRequest;
import java.math.BigDecimal;
import java.util.Objects;

/** Context #1: delegates price calculations without inspecting strategy types. */
public final class PricingContext {
    private PricingStrategy strategy;

    public PricingContext(PricingStrategy strategy) { setStrategy(strategy); }

    public void setStrategy(PricingStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "pricing strategy");
    }

    public BigDecimal quote(ParkingRequest request) { return strategy.calculate(request); }
}
