package less.stupid.flights.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

import java.util.UUID;

@Value
@JsonDeserialize
public class FlightSummary {

    public final UUID flightId;

    public final String callsign;

    @JsonCreator
    public FlightSummary(UUID flightId, String callsign) {
        this.flightId = flightId;
        this.callsign = callsign;
    }
}
