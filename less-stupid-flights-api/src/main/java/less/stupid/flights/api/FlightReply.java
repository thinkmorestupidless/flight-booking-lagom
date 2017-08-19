package less.stupid.flights.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

@Value
@JsonDeserialize
public class FlightReply {

    public final String flightId;

    @JsonCreator
    public FlightReply(String flightId) {
        this.flightId = flightId;
    }
}
