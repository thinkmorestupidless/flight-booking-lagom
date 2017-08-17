package less.stupid.flights.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import lombok.Value;

import java.util.UUID;

@Value
@JsonDeserialize
public class SelectSeat {

    public final UUID flightId;

    public final UUID passengerId;

    public final String seatAssignment;

    public SelectSeat(UUID flightId, UUID passengerId, String seatAssignment) {
        this.flightId       = Preconditions.checkNotNull(flightId, "flightId");
        this.passengerId    = Preconditions.checkNotNull(passengerId, "passengerId");
        this.seatAssignment = Preconditions.checkNotNull(seatAssignment, "seatAssignment");
    }
}
