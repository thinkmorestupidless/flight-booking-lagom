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

  @Override
  default Descriptor descriptor() {
    // @formatter:off
    return named("flights").withCalls(
        restCall(Method.POST, "/flights/add-flight", this::addFlight),
        restCall(Method.POST, "/flights/add-passenger", this::addPassenger),
        restCall(Method.POST, "/flights/select-seat", this::selectSeat),
        restCall(Method.POST, "/flights/remove-passenger/flight-id/:flightId/passengerId/:passengerId", this::removePassenger),
        restCall(Method.POST, "/flights/close-flight/flight-id/:flightId", this::closeFlight)
      ).withPathParamSerializer(UUID.class, PathParamSerializers.required("UUID", UUID::fromString, UUID::toString)
      ).withAutoAcl(true);
    // @formatter:on
  }
}
