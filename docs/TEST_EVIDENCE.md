# Test and build evidence

Java 21 was used to compile the Java 17-compatible source with `javac`.

- `java -cp out edu.aitu.parking.app.SmartParkingDemo`: completed successfully; see `DEMO_OUTPUT.txt`.
- The original before-strategy snapshot: **3/3 assertion checks passed** in an offline Java harness.
- The after-strategy snapshot: **32/32 assertion checks passed** in the same offline harness.
- `git diff before-strategy after-strategy -- src/test/java/edu/aitu/parking/PricingBehaviorRegressionTest.java`: empty output; preserved tests are byte-identical.
- Six JUnit 5 test classes contain 30 methods; one parameterized test generates three test invocations.

These checks ran through a local test harness that mimicked JUnit API assertions and invoked test methods via reflection; **they were not run by an actual JUnit 5 engine**. Maven and remote dependencies were unavailable during report creation. After importing the project in IntelliJ IDEA, run `mvn test` using the actual JUnit 5 dependency from `pom.xml`.

The assignment grader may also request a new Strategy, selector rule change, or sensor replacement: the architecture supports each exercise with limited changes.
