/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package less.stupid.flights.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
import com.lightbend.lagom.javadsl.api.transport.Method;

import java.util.Set;
import java.util.UUID;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

/**
 * The lessstupidflights service interface.
 * <p>
 * This describes everything that Lagom needs to know about how to serve and
 * consume the LessstupidflightsService.
 */
public interface FlightService extends Service {

  /**
   * Example: curl http://localhost:9000/api/hello/Alice
   */
  ServiceCall<Flight, String> addFlight();

  ServiceCall<Passenger, String> addPassenger();

  ServiceCall<SelectSeat, String> selectSeat();

  ServiceCall<NotUsed, String> removePassenger(UUID flightId, UUID passengerId);

  ServiceCall<NotUsed, String> closeFlight(UUID flightId);

  ServiceCall<NotUsed, Set<FlightSummary>> getAllFlights();

  @Override
  default Descriptor descriptor() {
    // @formatter:off
    return named("flights").withCalls(
        restCall(Method.GET, "/flights", this::getAllFlights),
        restCall(Method.POST, "/flights", this::addFlight),
        restCall(Method.POST, "/passengers", this::addPassenger),
        restCall(Method.PUT, "/passengers", this::selectSeat),
        restCall(Method.DELETE, "/passengers/:flightId/:passengerId", this::removePassenger),
        restCall(Method.DELETE, "/flights/:flightId", this::closeFlight)
      ).withPathParamSerializer(UUID.class, PathParamSerializers.required("UUID", UUID::fromString, UUID::toString)
      ).withAutoAcl(true);
    // @formatter:on
  }
}
