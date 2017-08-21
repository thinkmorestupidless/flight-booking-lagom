package less.stupid.flights.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

@Value
@JsonDeserialize
public class Passenger {

    public final UUID flightId;

    public final String lastName;

    public final String firstName;

    public final String initial;

    public final Optional<String> seatAssignment;

    public Passenger(UUID flightId, String lastName, String firstName, String initial, Optional<String> seatAssignment) {
        this.flightId       = Preconditions.checkNotNull(flightId, "flightId");
        this.lastName       = Preconditions.checkNotNull(lastName, "lastName");
        this.firstName      = Preconditions.checkNotNull(firstName, "firstName");
        this.initial        = Preconditions.checkNotNull(initial, "initial");
        this.seatAssignment = Preconditions.checkNotNull(seatAssignment, "seatAssignment");
    }

    public Passenger withSeatAssignment(Optional<String> seatAssignment) {
        return new Passenger(flightId, lastName, firstName, initial, seatAssignment);
    }
}
