# Smart Parking Garage - Assignment 5 (before refactoring)

This initial commit intentionally demonstrates the **bad approach**: `ParkingPriceEngine`
contains every pricing algorithm plus a conditional chain. The committed
`PricingBehaviorRegressionTest` specifies unchanged behavior for all three plans.

The next commit extracts the Strategy pattern. See the final README for the
complete project, UML, JUnit 5 suite, and live demonstration.
