package edu.aitu.parking.domain;

/** One garage space with fixed features plus a snapshot of current occupancy. */
public record ParkingSpot(String id, int level, int distanceMeters,
                          boolean chargingAvailable, boolean accessible, boolean occupied) {
    public ParkingSpot {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Spot ID is required");
        if (level < 0 || distanceMeters < 0) throw new IllegalArgumentException("Invalid spot location");
    }

    public boolean isSuitableFor(ParkingRequest request) {
        return !occupied
                && (!request.electricCharging() || chargingAvailable)
                && (!request.accessibleSpace() || accessible);
    }
}
