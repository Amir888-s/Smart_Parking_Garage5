package edu.aitu.parking.sensor;

/** External-data boundary; reports a current fraction between zero and one. */
public interface OccupancySource {
    double currentOccupancyRatio();
}
