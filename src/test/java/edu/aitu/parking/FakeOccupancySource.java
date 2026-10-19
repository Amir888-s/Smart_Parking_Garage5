package edu.aitu.parking;

import edu.aitu.parking.sensor.OccupancySource;

/** Test fake: returns a predictable reading instead of calling a sensor. */
final class FakeOccupancySource implements OccupancySource {
    private final double value;
    FakeOccupancySource(double value) { this.value = value; }
    @Override public double currentOccupancyRatio() { return value; }
}
