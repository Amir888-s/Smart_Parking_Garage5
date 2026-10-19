package edu.aitu.parking.sensor;

/** Mutable stand-in for a real garage sensor; no remote service is required. */
public final class SimulatedOccupancySource implements OccupancySource {
    private double ratio;

    public SimulatedOccupancySource(double ratio) { setOccupancyRatio(ratio); }

    public void setOccupancyRatio(double ratio) {
        if (!Double.isFinite(ratio) || ratio < 0 || ratio > 1)
            throw new IllegalArgumentException("Occupancy must be in [0, 1]");
        this.ratio = ratio;
    }

    @Override public double currentOccupancyRatio() { return ratio; }
}
