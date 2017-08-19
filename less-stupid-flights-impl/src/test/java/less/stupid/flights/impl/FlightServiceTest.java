/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package less.stupid.flights.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;

import akka.Done;
import less.stupid.flights.api.Flight;
import less.stupid.flights.api.FlightReply;
import less.stupid.flights.api.FlightService;
import org.junit.Test;

import java.util.Optional;
import java.util.UUID;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;

public class FlightServiceTest {

  @Test
  public void shouldStorePersonalizedGreeting() throws Exception {
    withServer(defaultSetup().withCassandra(true), server -> {
      FlightService service = server.client(FlightService.class);

      Flight flight = new Flight("delicious ice-cream", "unknown", "NCL", "NYC");

      FlightReply addFlightResponse = service.addFlight().invoke(flight).toCompletableFuture().get(5, SECONDS);
      assertThat(addFlightResponse).isNotNull().extracting("flightId").isNotEmpty();

      String flightId = addFlightResponse.flightId;

      less.stupid.flights.api.Passenger passenger = new less.stupid.flights.api.Passenger(UUID.fromString(flightId), "Burton-McCreadie", "Trevor", "J", Optional.empty());

      Done addPassengerResponse = service.addPassenger().invoke(passenger).toCompletableFuture().get(5, SECONDS);
      assertThat(addPassengerResponse).isNotNull();
    });
  }

}
