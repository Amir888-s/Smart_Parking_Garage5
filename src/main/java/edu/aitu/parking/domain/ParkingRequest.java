package edu.aitu.parking.domain;

import java.util.Objects;

/** Immutable details of one driver's parking request. */
public record ParkingRequest(long stayMinutes, Membership membership,
                             boolean electricCharging, boolean accessibleSpace) {
    public ParkingRequest {
        if (stayMinutes <= 0) throw new IllegalArgumentException("Stay must be positive");
        Objects.requireNonNull(membership, "membership");
    }
}
