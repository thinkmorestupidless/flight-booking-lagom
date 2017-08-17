package less.stupid.flights.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

@Value
@JsonDeserialize
public class FlightReply {
    public final String reply;

    @JsonCreator
    public FlightReply(String reply) {
        this.reply = reply;
    }
}
