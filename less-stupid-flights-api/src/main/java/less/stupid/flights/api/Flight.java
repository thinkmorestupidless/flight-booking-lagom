package less.stupid.flights.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import lombok.Value;

@Value
@JsonDeserialize
public class Flight {

    public final String callsign;

    public final String equipment;

    public final String departureIata;

    public final String arrivalIata;

    @JsonCreator
    public Flight(String callsign, String equipment, String departureIata, String arrivalIata) {
        this.callsign       = Preconditions.checkNotNull(callsign, "callsign");
        this.equipment      = Preconditions.checkNotNull(equipment, "equipment");
        this.departureIata  = Preconditions.checkNotNull(departureIata, "departureIata");
        this.arrivalIata    = Preconditions.checkNotNull(arrivalIata, "arrivalIata");
    }
}
