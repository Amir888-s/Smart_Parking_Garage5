# Smart Parking Garage

**Assignment 5 — Strategy Pattern** | Java 17+, Maven, JUnit 5 | Currency: KZT

Smart Parking Garage calculates parking fees and selects available spaces using two independent Strategy families. The active algorithm can be replaced while the program runs. No real sensors, credentials, internet service, or database are required.

## Assignment requirements coverage

| Assignment rule | Where demonstrated |
|---|---|
| Two Strategy families, three genuinely distinct algorithms per family | `pricing/*Pricing.java`; `allocation/*Strategy.java` |
| One constructor-injected, runtime-switchable context per family | `PricingContext`; `AllocationContext`; both expose `setStrategy(...)` |
| Automatic, separate selector for each family | `PricingSelector`; `AllocationSelector` |
| Before/after Git refactoring, preserved regression tests | Tags `before-strategy`, `after-strategy`; `PricingBehaviorRegressionTest` |
| External-data abstraction with a fake implementation | `OccupancySource`; `SimulatedOccupancySource`; test-only `FakeOccupancySource` |
| JUnit 5 tests covering strategies, selector rules, parameterization | Six test classes under `src/test/java` |
| Same input, manual selection, auto selection, runtime swap | `SmartParkingDemo.main()` |
| UML, SRP responsibilities, Strategy comparison, commit information | `docs/uml/`; class table below; this README; `docs/COMMITS.md` |

## Run

In IntelliJ IDEA, open **this folder** as a Maven project; run `SmartParkingDemo.main()` or the unit tests.

```bash
mvn test
mvn exec:java
```

With Java installed but no Maven, run the main demo from the project root:

```bash
mkdir -p out
javac -d out $(find src/main/java -name '*.java')
java -cp out edu.aitu.parking.app.SmartParkingDemo
```

For Windows without Maven, double-click `run-demo.bat` (JDK 17+ required).

## Algorithms: what makes their logic genuinely different?

**Pricing family (`PricingStrategy`)**

| Concrete strategy | Algorithm | 120-minute example, occupancy 55% |
|---|---|---|
| `HourlyPricing` | Round the stay upward to whole hours; charge KZT 500 per started hour. | KZT 1,000 |
| `DailyCapPricing` | Partition the stay into separate 24-hour blocks; cap each block at KZT 3,000. | KZT 1,000 |
| `DemandPricing` | Charge a KZT 600 entry fee plus KZT 250 per started 30-minute slot; multiply by occupancy tier. | KZT 1,600 |

The demand multiplier is 1.0 below 70% occupied, 1.2 from 70% through below 85%, 1.5 from 85% upward. These are **different algorithms**, rather than changing only one price constant.

**Allocation family (`SpotAllocationStrategy`)**

| Concrete strategy | Algorithm |
|---|---|
| `NearestEntranceStrategy` | Choose the shortest walking distance among suitable free spaces. |
| `LowestLevelStrategy` | Choose the lowest level first, then break ties by walking distance. |
| `LeastCrowdedLevelStrategy` | Count occupied/free spaces per level; prioritize the greatest proportion of free spaces, then level, distance, ID. |

Every allocation strategy ignores occupied spaces, respects charging requirements, respects accessible-space requirements, returns `Optional.empty()` when nothing fits.

## Automatic selector rules (priority matters)

`PricingSelector.select(request)` evaluates rules in this order:

1. VIP stay of **at least 180 minutes**: `DailyCapPricing`.
2. Otherwise, occupancy **at least 85%**: `DemandPricing`.
3. Otherwise, stay of **at least 360 minutes**: `DailyCapPricing`.
4. Otherwise: `HourlyPricing`.

`AllocationSelector.select(request)` evaluates rules in this order:

1. Accessible space requested: `NearestEntranceStrategy`.
2. Otherwise, occupancy **at least 80%**: `LeastCrowdedLevelStrategy`.
3. Otherwise, VIP member: `NearestEntranceStrategy`.
4. Otherwise: `LowestLevelStrategy`.

Conditional selection belongs **only to selectors**, never to a context. A strategy's own algorithm may naturally contain decisions (tiered fees, comparisons).

## Full class responsibility table (SRP)

Each entry below is a single sentence with fewer than 25 words, avoiding the conjunctions prohibited by the assignment. Package paths are below `edu.aitu.parking`.

| Package | Class / interface / enum | Single responsibility |
|---|---|---|
| `domain` | `Membership` | Represents the supported membership tiers. |
| `domain` | `ParkingRequest` | Holds immutable information about one requested stay. |
| `domain` | `ParkingSpot` | Represents one parking space with eligibility data. |
| `sensor` | `OccupancySource` | Defines the interface for garage occupancy readings. |
| `sensor` | `SimulatedOccupancySource` | Provides adjustable occupancy readings without an external API. |
| `pricing` | `PricingPlan` | Identifies the supported manual pricing plans. |
| `pricing` | `PricingStrategy` | Defines the contract for one pricing algorithm. |
| `pricing` | `HourlyPricing` | Calculates prices using started hourly units. |
| `pricing` | `DailyCapPricing` | Calculates prices using capped daily blocks. |
| `pricing` | `DemandPricing` | Calculates occupancy-adjusted prices using half-hour units. |
| `pricing` | `PricingContext` | Delegates price calculation to the current pricing strategy. |
| `pricing` | `PricingSelector` | Selects a pricing algorithm from customer demand conditions. |
| `pricing` | `ParkingPriceEngine` | Maps manual plan choices to registered pricing strategies. |
| `allocation` | `SpotAllocationStrategy` | Defines the contract for allocating one parking space. |
| `allocation` | `NearestEntranceStrategy` | Selects the suitable spot with minimum walking distance. |
| `allocation` | `LowestLevelStrategy` | Selects the suitable spot on the lowest garage floor. |
| `allocation` | `LeastCrowdedLevelStrategy` | Selects a spot from the floor with maximum free-space share. |
| `allocation` | `AllocationContext` | Delegates space assignment to the active allocation strategy. |
| `allocation` | `AllocationSelector` | Selects a space-allocation algorithm using current parking conditions. |
| `app` | `SmartParkingDemo` | Demonstrates both strategy families through printed console scenarios. |
| `test` | `FakeOccupancySource` | Supplies fixed occupancy readings for deterministic tests. |
| `test` | `PricingBehaviorRegressionTest` | Protects existing pricing behavior throughout refactoring. |
| `test` | `PricingStrategiesTest` | Verifies pricing algorithms across representative duration cases. |
| `test` | `PricingSelectorTest` | Checks priority rules used for automatic pricing. |
| `test` | `AllocationStrategiesTest` | Checks parking-space algorithms using predefined garage layouts. |
| `test` | `AllocationSelectorTest` | Checks priority rules used for automatic space allocation. |
| `test` | `ContextsAndSourcesTest` | Checks runtime swapping through context objects. |

## Why Strategy, not State or Template Method?

The two families model **interchangeable algorithms for a single task**. Each concrete pricing strategy answers one question: “How much does this stay cost?” Each concrete allocation strategy answers another: “Which suitable free spot should be assigned?” Both contexts store an interface reference, delegate the work, then let a caller replace that reference with `setStrategy()`. A caller may explicitly choose an algorithm; a dedicated selector may choose one automatically based on the occupancy reading, membership, or accessibility request. Changing an algorithm does not require changing the context's own code. A seventh algorithm could implement the relevant interface, then be injected into its context directly.

This is **not State**, because the concrete strategies do not model stages in a machine's lifecycle. An assigned parking spot does not turn a context from one internal lifecycle state into another; a requested algorithm simply replaces the previous implementation. No strategy is allowed to silently initiate a transition to the next strategy. The selection decision stays in the caller or selector.

This is **not Template Method**, because the common algorithm is not implemented as a fixed sequence in a parent class. There is no abstract superclass with an unchangeable template calling overridable steps. Instead, each algorithm is an independent class implementing a small interface. `PricingContext` and `AllocationContext` use composition and delegation, so algorithm choice can change at runtime without subclassing either context.

The design follows the Open/Closed Principle for the **contexts**: new behaviors extend their strategy interfaces without modifying those contexts. Extending the automatic decision policy may still require changing a selector rule, which is deliberately isolated inside the corresponding selector.

## Git refactoring history

The project ZIP **includes `.git`**, keeping local commits and tags rather than merely claiming refactoring happened.

- `before-strategy`: `ParkingPriceEngine` holds hourly, capped, demand algorithms in one `if / else` chain.
- `after-strategy`: `ParkingPriceEngine` delegates to `PricingContext` plus distinct `PricingStrategy` implementations.
- The file `src/test/java/edu/aitu/parking/PricingBehaviorRegressionTest.java` is unchanged across these tags.
- Full local commit hashes, evidence, instructions for real GitHub commit URLs: [docs/COMMITS.md](docs/COMMITS.md).

Note: GitHub links only become real after you push this repository to **your own GitHub remote**. No remote URL has been invented.

## Documentation

- [Complete defense guide: classes, methods, examples, questions](docs/DEFENSE_GUIDE.md)
- [Full UML diagram](docs/uml/full-project.png)
- [Pricing UML](docs/uml/pricing-family.png)
- [Allocation UML](docs/uml/allocation-family.png)
- [Editable UML source](docs/uml/smart-parking-garage.puml)
- [Commit evidence](docs/COMMITS.md)
- [Test/build evidence](docs/TEST_EVIDENCE.md)
- [Printable report (PDF)](docs/Smart_Parking_Garage_Report.pdf)
- [Editable report (Word)](docs/Smart_Parking_Garage_Report.docx)

## Submission checklist

- [x] 2 Strategy interfaces, 3 concrete implementations each
- [x] 2 contexts with constructor injection, `setStrategy()`, pure delegation
- [x] 2 selectors with explicit priority rules
- [x] External sensor boundary plus deterministic fake
- [x] JUnit 5 strategy tests, selector tests, parameterized tests
- [x] Console demo; manual selection; auto selection; live switching
- [x] Before/after Git commits; unchanged regression test source
- [x] SRP table, UML, written Strategy comparison, defense guide
- [ ] Push the existing commits to your GitHub repository so public URLs become available
- [ ] Run `mvn test` on your computer with Maven access (this build environment has no Maven/JUnit binaries)
