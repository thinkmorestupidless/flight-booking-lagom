/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package less.stupid.flights.impl;

import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import less.stupid.flights.api.Flight;
import less.stupid.flights.api.FlightService;
import less.stupid.flights.api.FlightSummary;
import less.stupid.flights.api.SelectSeat;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static less.stupid.utils.Await.await;
import static org.assertj.core.api.Assertions.assertThat;

public class FlightServiceTest {

  private static final String callsign  = "UA100";
  private static final String equipment = "757-800";
  private static final String departure = "EWR";
  private static final String arrival   = "SFO";
  private static final Flight flight = new Flight(callsign, equipment, departure, arrival);

  private static final FiniteDuration wait = new FiniteDuration(5, SECONDS);

  private final static ServiceTest.Setup setup = defaultSetup().withCassandra(true)
          .configureBuilder(b ->
                  b.configure("cassandra-query-journal.eventual-consistency-delay", "0")
          );

  private void withService(Consumer<FlightService> block) {

    withServer(setup, server -> {
      block.accept(server.client(FlightService.class));
    });
  }

  @Test
  public void shouldGetAllFlights() {

    withService(service -> {

      String response = await(service.addFlight().invoke(flight));

      eventually(wait, () -> {
        Set<FlightSummary> flights = await(service.getAllFlights().invoke());

        assertThat(flights).hasSize(1);
      });
    });
  }

  @Test
  public void shouldAddFlight() {

    withService(service -> {
      assertThat(await(service.addFlight().invoke(flight)).startsWith("OK"));
    });
  }

  @Test
  public void shouldAddPassenger() {

    withService(service -> {

      String flightResponse = await(service.addFlight().invoke(flight));
      UUID flightId = idFromString(flightResponse);

      String passengerResponse = await(service.addPassenger().invoke(passenger(flightId)));

      assertThat(passengerResponse).startsWith("OK");
    });
  }

  @Test
  public void shouldRemovePassenger() {

    withService(service -> {

      String flightResponse = await(service.addFlight().invoke(flight));
      UUID flightId = idFromString(flightResponse);

      String passengerResponse = await(service.addPassenger().invoke(passenger(flightId)));
      UUID passengerId = idFromString(passengerResponse);

      String response = await(service.removePassenger(flightId, passengerId).invoke());

      assertThat(response).isEqualTo("OK");
    });
  }

  @Test
  public void shouldSelectSeat() {

    withService(service -> {

      String flightResponse = await(service.addFlight().invoke(flight));
      UUID flightId = idFromString(flightResponse);

      String passengerResponse = await(service.addPassenger().invoke(passenger(flightId)));
      UUID passengerId = idFromString(passengerResponse);

      String response = await(service.selectSeat().invoke(new SelectSeat(flightId, passengerId, "13C")));

      assertThat(response).isEqualTo("OK");
    });
  }

  @Test
  public void shouldCloseFlight() {

    withService(service -> {

      String flightResponse = await(service.addFlight().invoke(flight));
      UUID flightId = idFromString(flightResponse);

      String response = await(service.closeFlight(flightId).invoke());

      assertThat(response).isEqualTo("OK");
    });
  }

  private less.stupid.flights.api.Passenger passenger(UUID flightId) {
    return new less.stupid.flights.api.Passenger(flightId, "Walsh", "Sean", "A", Optional.of("1A"));
  }

  private UUID idFromString(String id) { return UUID.fromString(id.split(":")[1]); }
}
