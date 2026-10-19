# Smart Parking Garage: complete explanation for defense

## 1. Your assignment, explained from zero

The teacher asks you to demonstrate the **Strategy design pattern** on an original domain. You need two different operations, each with at least three different algorithms. Do not write one large function that chooses an algorithm using conditionals. Put each algorithm in a separate class implementing a common interface. A small **context** stores an interface reference; its `setStrategy()` changes the algorithm during execution. A separate **selector** contains the conditional rules for choosing automatically. One algorithm must read outside information through your own interface. You also need JUnit 5 tests, a parameterized test, preserved before/after Git history, a runnable demo, a class-responsibility table, a UML diagram, plus an explanation of Strategy versus State versus Template Method.

Our chosen domain is a multi-floor parking garage. There are **two independent jobs**: (1) calculate the customer's bill, (2) find a suitable free parking space. A change in the pricing policy should not force a change to the spot allocation policy.

## 2. Foundation: six terms to know

- **Interface:** A Java contract listing methods that implementing classes promise to provide.
- **Strategy:** A class implementing one version of an algorithm.
- **Strategy family:** An interface plus multiple classes solving the same kind of problem differently.
- **Context:** A class keeping a strategy instance. It delegates the operation to that instance.
- **Selector:** A class examining inputs to choose a strategy; its `if` statements are permitted by the assignment.
- **Dependency injection:** Supplying a collaborator via a constructor instead of hardcoding the collaborator inside a class.

**`implements` vs `extends`:** `HourlyPricing implements PricingStrategy` means the class fulfills the interface. This project uses no strategy superclass, so we are using composition, not inheritance of algorithms.

## 3. Program structure

```text
Smart_Parking_Garage/
├── pom.xml                         Maven build, Java 17, JUnit 5
├── README.md                       Assignment checklist, SRP table, design comparison
├── run-demo.bat                    Demo launcher for Windows
├── src/main/java/edu/aitu/parking/
│   ├── app/SmartParkingDemo.java   Entry point and demonstration
│   ├── domain/                      Input data and spot model
│   ├── sensor/                      Sensor abstraction and simulated source
│   ├── pricing/                     Pricing family, context, selector, manual engine
│   └── allocation/                  Parking-space family, context, selector
├── src/test/java/edu/aitu/parking/ Six JUnit test classes plus fake sensor
└── docs/
    ├── DEFENSE_GUIDE.md            This guide
    ├── COMMITS.md                  Actual local hashes
    └── uml/                        PNG diagrams plus PlantUML source
```

The first project level contains build/documentation files. `src/main/java` contains runtime code. `src/test/java` contains tests. Packages prevent naming conflicts while grouping related classes.

## 4. Explanation of EVERY production file

### 4.1 `domain/Membership.java`

An `enum` with `REGULAR` plus `VIP`. Using an enum prevents random strings such as `"VPI"`. Selectors can use membership as a rule input.

### 4.2 `domain/ParkingRequest.java`

An immutable Java `record` with `stayMinutes`, `membership`, `electricCharging`, `accessibleSpace`. The compact constructor rejects non-positive stays plus missing membership. Java automatically generates accessors such as `request.stayMinutes()`. The same request is passed to every algorithm during the demo. `electricCharging=true` means the selected space must have charging equipment; `accessibleSpace=true` means it must be accessible.

### 4.3 `domain/ParkingSpot.java`

A `record` with space ID, level, walking distance, equipment flags, accessibility flag, occupancy flag. `isSuitableFor(request)` returns true only when the space is free plus meets charging/accessibility requirements. The three allocation strategies reuse this one filter, avoiding duplicate suitability logic. This is a snapshot, not a database-backed reservation engine.

### 4.4 `sensor/OccupancySource.java`

An interface with one method: `double currentOccupancyRatio()`. A reading of `0.85` means 85% occupied. Strategies depend on this small contract instead of depending on a real sensor or HTTP client. Replacing sensors requires a different implementation, not a change to `DemandPricing`.

### 4.5 `sensor/SimulatedOccupancySource.java`

A mutable implementation of `OccupancySource`. Constructor takes an initial ratio. `setOccupancyRatio(value)` changes that ratio at runtime, rejecting values outside 0..1 plus NaN/Infinity. Its `currentOccupancyRatio()` returns the stored value. The console demo changes it from 55% to 92% to visibly trigger a different strategy.

### 4.6 `pricing/PricingPlan.java`

An enum with `HOURLY`, `DAILY_CAP`, `DEMAND`. This identifies a user's explicit/manual choice when using `ParkingPriceEngine`. It does not itself contain pricing logic.

### 4.7 `pricing/PricingStrategy.java` — pricing interface

Declares `BigDecimal calculate(ParkingRequest request)`. Each pricing class implements the same signature. `BigDecimal` is preferred to `double` for representing currency amounts precisely.

### 4.8 `pricing/HourlyPricing.java`

Counts started 60-minute units, charging **KZT 500 each**. `Math.floorDiv(minutes - 1, 60) + 1` is a safe ceiling for positive integer minutes. Example: 61 minutes gives 2 billable hours, so 1,000 KZT. The calculation is not influenced by occupancy.

### 4.9 `pricing/DailyCapPricing.java`

Loops over the stay in blocks of up to 1,440 minutes (24 hours). For each block, it calculates the hourly charge but limits it to **KZT 3,000**. Adds all block totals. Example: 26 hours costs 3,000 KZT for the first day plus 1,000 KZT for the remaining two hours = **4,000 KZT**. This is different logic from merely picking a lower hourly rate.

### 4.10 `pricing/DemandPricing.java`

Receives an `OccupancySource` in its constructor. First computes entry fee **600 KZT** plus **250 KZT per started half-hour**. Next reads occupancy. Multipliers: below 70% = 1.0; from 70% to below 85% = 1.2; at least 85% = 1.5. Finally rounds to a whole tenge. For 60 minutes, base = 600 + (2 x 250) = 1,100; at 90% occupancy, price = 1,100 x 1.5 = **1,650 KZT**. This is the required dependency-isolated strategy.

### 4.11 `pricing/PricingContext.java` — pricing context

Its private field is `PricingStrategy strategy`. The constructor immediately calls `setStrategy(strategy)`. `setStrategy()` accepts another implementation, rejecting null. `quote(request)` only runs `strategy.calculate(request)`. **There are no `if`, `switch`, or `instanceof` decisions about strategy types inside this context.** It never needs editing when a new pricing implementation is added.

### 4.12 `pricing/PricingSelector.java` — pricing selector

Owns references to the three pricing algorithms plus an `OccupancySource`. `select(request)` chooses: VIP staying at least 180 min -> capped; else occupancy >=85% -> demand; else stay >=360 min -> capped; otherwise -> hourly. Check rules in that order because priorities matter. The selector **returns a `PricingStrategy` object**; the caller injects that into `PricingContext`.

### 4.13 `pricing/ParkingPriceEngine.java` — manual facade plus refactoring evidence

This class keeps the same constructor and `quote(request, plan)` public method in both Git commits. **Before:** its `quote` method contained a large `if / else` chain implementing all three pricing algorithms. **After:** it stores an enum-to-strategy `Map`, creates `PricingContext`, chooses a registered strategy by `plan`, then asks the context to delegate. The unchanged regression tests call the same API on both revisions. The manual mapping is distinct from the automatic `PricingSelector` rules.

### 4.14 `allocation/SpotAllocationStrategy.java` — allocation interface

Declares `Optional<ParkingSpot> choose(ParkingRequest request, List<ParkingSpot> spaces)`. The `Optional` may contain a chosen spot or be empty when no suitable free space exists. It prevents unsafe use of a `null` result.

### 4.15 `allocation/NearestEntranceStrategy.java`

Filters spaces using `isSuitableFor(request)` then chooses the minimum `distanceMeters`. If two distances match, it uses the ID for deterministic ordering. Works well for accessibility or VIP convenience.

### 4.16 `allocation/LowestLevelStrategy.java`

Filters suitable spaces, then chooses the smallest level. Within that level it prefers the shortest walk, finally the ID. Ground-level preference is its primary goal; distance is only a secondary criterion.

### 4.17 `allocation/LeastCrowdedLevelStrategy.java`

Groups spaces by level to compute total spaces plus currently free spaces. Calculates each floor's `free / total` ratio, then prefers suitable spaces from the floor with the **highest percentage of free spaces**. Ties are resolved by lower level, shorter distance, ID. Example: floor 0 has one free out of three (33%), floor 1 has three free out of three (100%): choose floor 1. Note that occupancy summaries consider all spots, while the final candidates also satisfy the current driver's specific features.

### 4.18 `allocation/AllocationContext.java` — allocation context

Stores `SpotAllocationStrategy strategy`; its constructor accepts the initial implementation. `setStrategy()` enables replacement. `allocate(request, spots)` delegates to `strategy.choose(request, spots)` without checking concrete types.

### 4.19 `allocation/AllocationSelector.java` — allocation selector

Chooses `NearestEntranceStrategy` for accessibility first. If not, chooses `LeastCrowdedLevelStrategy` at least 80% occupancy. If not, VIP gets nearest. Regular traffic defaults to lowest level. The rules are tested individually, including overlapping conditions to prove priority.

### 4.20 `app/SmartParkingDemo.java` — entry point

`main(String[] args)` builds one simulated sensor, one sample request, strategy objects, contexts, selectors, plus several parking spaces. It prints results from **all three pricing strategies** on the same request, then does the same for **all three space strategies**. It shows a manual strategy choice, automatic selection, a high-occupancy update, plus a runtime swap without recreating the contexts.

## 5. Test classes explained

- `FakeOccupancySource`: Returns a fixed ratio for deterministic tests; implements the same interface as the simulated production source.
- `PricingBehaviorRegressionTest`: **Three unchanged tests across both Git tags.** Verifies rounding, daily cap, occupancy surcharge through the stable `ParkingPriceEngine` API.
- `PricingStrategiesTest`: Tests each price algorithm, the three occupancy tiers, plus one `@ParameterizedTest` fed by `@MethodSource` to verify non-negative output for all three implementations.
- `PricingSelectorTest`: Covers short stay, long stay, demand threshold, VIP protection, VIP short-stay precedence.
- `AllocationStrategiesTest`: Tests each of three allocation approaches, suitable-space filters, absence of a suitable space.
- `AllocationSelectorTest`: Verifies ordinary/VIP defaults, congestion threshold, accessibility precedence, congestion-over-VIP precedence.
- `ContextsAndSourcesTest`: Tests `setStrategy()` at runtime in both contexts, fake-source swapping, the simulated source, validation, null rejection.

The project contains **30 JUnit test methods** producing **32 test invocations** (the parameterized test runs three times). The offline assertion harness completed all 32. Actual JUnit 5 execution requires Maven to download its declared dependencies.

## 6. Step-by-step explanation of the important Java syntax

```java
PricingStrategy strategy = new HourlyPricing();
```
The left side is the **interface type**; the right side creates one concrete implementation. Java allows this because `HourlyPricing implements PricingStrategy`.

```java
PricingContext context = new PricingContext(strategy);
context.quote(request);
context.setStrategy(new DailyCapPricing());
context.quote(request);
```
Both `quote()` calls use the **same context instance**, but the underlying algorithm changes after `setStrategy()`.

```java
context.setStrategy(selector.select(request));
```
The selector decides which object to use; the context delegates without knowing why that object was picked.

```java
return spaces.stream().filter(s -> s.isSuitableFor(request)).min(comparator);
```
`stream()` starts processing a list; `filter` retains eligible spots; lambda `s -> ...` means “for each spot s”; `min(comparator)` finds the best candidate. It returns an `Optional`.

```java
Map<Integer, Long> totalByLevel = spaces.stream()
    .collect(Collectors.groupingBy(ParkingSpot::level, Collectors.counting()));
```
Builds a map such as `{0=3, 1=4}`, counting all parking spots per level. The least-crowded strategy compares free counts with these totals.

```java
private final OccupancySource occupancySource;
```
This field depends on the interface rather than on a specific source. A replacement fake/test source can provide the same operation.

## 7. Refactoring explanation: before vs after

**Before tag** `before-strategy`: a single `ParkingPriceEngine.quote()` method contains every pricing formula plus a conditional chain on `PricingPlan`. This is difficult to extend because a new plan forces changes to the large method.

**After tag** `after-strategy`: `PricingStrategy` is extracted. Three separate pricing classes implement it. `PricingContext` delegates the calculation. `ParkingPriceEngine.quote()` only looks up a strategy, swaps the context's reference, then delegates. The regression test file remains byte-identical across both tags.

Demonstrate in terminal:

```bash
git log --oneline --decorate
git show before-strategy:src/main/java/edu/aitu/parking/pricing/ParkingPriceEngine.java
git show after-strategy:src/main/java/edu/aitu/parking/pricing/ParkingPriceEngine.java
git diff before-strategy after-strategy -- src/main/java/edu/aitu/parking/pricing/ParkingPriceEngine.java
git diff before-strategy after-strategy -- src/test/java/edu/aitu/parking/PricingBehaviorRegressionTest.java
```

The final `git diff` should produce **no output**, confirming the initial tests were unchanged. The actual local hashes live in `docs/COMMITS.md`; public GitHub URLs require pushing the repository first.

## 8. Live coding during defense: likely tasks

**A. Add another strategy.** Write `WeekendPricing implements PricingStrategy`; implement `calculate(request)` with its own business logic. Construct `new WeekendPricing()`; inject it using `pricingContext.setStrategy(new WeekendPricing())`. The context stays untouched. If asked to select it automatically, extend only `PricingSelector` with a clear weekend input/rule plus test. A new rule necessarily updates the selector, not the context.

**B. Change the selector rule.** Suppose congestion should trigger `LeastCrowdedLevelStrategy` at 70%, not 80%. Change `.80` to `.70` inside `AllocationSelector.select()`; adjust the boundary-case selector tests. **Never** add strategy-type conditionals to `AllocationContext`.

**C. Swap the sensor.** Implement `OccupancySource` in `ExternalOccupancyAdapter`, returning a valid ratio. Pass that source to `new DemandPricing(source)` plus the selectors. Or pass `new FakeOccupancySource(.95)` to test the surcharge predictably. The `DemandPricing` class does not change.

**D. Spot is unavailable.** Add a spot with `occupied=true`. All strategies filter it. When every spot is occupied or feature-incompatible, the result is `Optional.empty()`.

**E. Explain Open/Closed Principle.** The context does not change when a new strategy arrives, because it calls the shared interface. The selector may need a new rule by design; automatic policy lives in one place.

## 9. Questions the teacher may ask, with compact answers

**Q: What is Strategy?** A pattern that encapsulates interchangeable algorithms behind the same interface.

**Q: Which are the two families?** Price calculation (`PricingStrategy`) plus parking-space selection (`SpotAllocationStrategy`).

**Q: Why different algorithms, not only different numbers?** Hourly rounding, capped daily partitioning, dynamic entry-fee tiers; minimum-distance, floor-priority, occupancy-balanced selection.

**Q: Why is a context needed?** It owns the currently selected algorithm and delegates work to it.

**Q: Where does the automatic `if` go?** Only in the corresponding selector.

**Q: Why use `setStrategy()`?** To swap algorithm objects at runtime without rebuilding the context.

**Q: Why two interfaces?** Price calculation and space allocation have different input/output contracts.

**Q: What does dependency isolation mean?** Business logic depends on `OccupancySource`, not on a sensor implementation.

**Q: How do tests avoid actual sensor data?** `FakeOccupancySource` supplies a fixed value.

**Q: What is `@ParameterizedTest`?** A JUnit test invoked repeatedly with distinct supplied parameters.

**Q: What is SRP?** Each class has one clear responsibility.

**Q: Why is this not State?** Algorithms are selected for policy, not lifecycle transitions.

**Q: Why is this not Template Method?** The algorithms do not inherit a fixed template algorithm from a superclass.

**Q: What happens on `Optional.empty()`?** The system reports no eligible free parking space; it does not fabricate a booking.

**Q: Are prices real commercial rates?** No, numeric values are intentionally simulated for an educational project.

**Q: Is the garage persistence implemented?** No; the assignment grades interchangeable algorithms rather than a production booking/database system.

## 10. Short presentation script (2–3 minutes)

“My project is Smart Parking Garage. I implemented the Strategy design pattern for two operations. The first operation is pricing. My interface `PricingStrategy` has three implementations: hourly pricing, daily-cap pricing, and demand-based pricing. The second operation is choosing a parking space. The interface `SpotAllocationStrategy` has three implementations: nearest entrance, lowest level, and least crowded level.

Each family has a context. The context receives an interface in the constructor, calls it to do the work, and changes it using `setStrategy()`. My contexts do not contain `if` or `switch` statements for choosing strategy types. Automatic decisions are inside `PricingSelector` and `AllocationSelector`.

The demand-based price reads occupancy through `OccupancySource`. I can switch from the simulated source to a fixed fake without editing the strategy. I wrote JUnit 5 tests for every strategy, each selector rule, plus a parameterized test. I also committed the original pricing `if / else` implementation before extracting the strategies. The three old regression tests are unchanged between commits. Now I will run the console demo showing all strategies with the same input, manual and automatic decisions, plus runtime switching.”
