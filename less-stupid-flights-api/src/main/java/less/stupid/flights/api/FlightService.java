/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package less.stupid.flights.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;
import static com.lightbend.lagom.javadsl.api.Service.topic;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.broker.kafka.KafkaProperties;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
import com.lightbend.lagom.javadsl.api.transport.Method;

import java.util.UUID;

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

  ServiceCall<Passenger, Done> addPassenger();

  ServiceCall<SelectSeat, Done> selectSeat();

  ServiceCall<NotUsed, Done> removePassenger(UUID flightId, UUID passengerId);

  ServiceCall<NotUsed, Done> closeFlight(UUID flightId);

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
