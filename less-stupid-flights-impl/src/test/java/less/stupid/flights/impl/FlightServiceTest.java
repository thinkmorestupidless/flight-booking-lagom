/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package less.stupid.flights.impl;

import less.stupid.flights.api.Flight;
import less.stupid.flights.api.FlightService;
import less.stupid.flights.api.Passenger;
import less.stupid.flights.api.SelectSeat;
import org.junit.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class FlightServiceTest {

  private static final String callsign  = "UA100";
  private static final String equipment = "757-800";
  private static final String departure = "EWR";
  private static final String arrival   = "SFO";

  @Test
  public void shouldAddFlight() {
    withServiceAndFlight((service, reply) -> assertThat(reply).startsWith("OK"));
  }

  @Test
  public void shouldAddPassenger() {
    withServiceAndFlightId((service, flightId) -> {
      try {
        String response = service.addPassenger()
                                 .invoke(new Passenger(flightId, "Walsh", "Sean", "A", Optional.of("1A")))
                                 .toCompletableFuture().get(5, SECONDS);

        assertThat(response).startsWith("OK");
      } catch (Exception e) {
        fail("problem adding passenger - {}", e.getMessage());
      }
    });
  }

  @Test
  public void shouldRemovePassenger() {
    withServiceFlightAndPassenger(sfp -> {
      try {
        String response = sfp.service.removePassenger(sfp.flightId, sfp.passengerId)
                                     .invoke()
                                     .toCompletableFuture()
                                     .get(5, SECONDS);

        assertThat(response).isEqualTo("OK");
      } catch (Exception e) {
        fail("problem removing passenger - {}", e.getMessage());
      }
    });
  }

  @Test
  public void shouldSelectSeat() {
    withServiceFlightAndPassenger(sfp -> {
      try {
        String response = sfp.service.selectSeat()
                                     .invoke(new SelectSeat(sfp.flightId, sfp.passengerId, "13C"))
                                     .toCompletableFuture()
                                     .get(5, SECONDS);

        assertThat(response).isEqualTo("OK");
      } catch (Exception e) {
        fail("problem selecting seat - {}", e.getMessage());
      }
    });
  }

  @Test
  public void shouldCloseFlight() {
    withServiceFlightAndPassenger(sfp -> {
      try {
        String response = sfp.service.closeFlight(sfp.flightId)
                                     .invoke()
                                     .toCompletableFuture()
                                     .get(5, SECONDS);

        assertThat(response).isEqualTo("OK");
      } catch (Exception e) {
        fail("problem closing flight - {}", e.getMessage());
      }
    });
  }

  private void withService(Consumer<FlightService> block) {
    withServer(defaultSetup().withCassandra(true), server -> block.accept(server.client(FlightService.class)));
  }

  private void withServiceAndFlight(BiConsumer<FlightService, String> block) {
    withService(service -> {
      try {
        block.accept(service, service.addFlight()
                                     .invoke(new Flight(callsign, equipment, departure, arrival))
                                     .toCompletableFuture()
                                     .get(5, SECONDS));
      } catch (Exception e) {
        fail("problem adding flight - {}", e.getMessage());
      }
    });
  }

  private void withServiceAndFlightId(BiConsumer<FlightService, UUID> block) {
    withServiceAndFlight((service, flight) -> {
      block.accept(service, idFromString(flight));
    });
  }

  private void withServiceFlightAndPassenger(Consumer<ServiceFlightAndPassenger> block) {
    withServiceAndFlightId((service, flightId) -> {
      try {
        String response = service.addPassenger()
                                 .invoke(new Passenger(flightId, "Walsh", "Sean", "A", Optional.of("1A")))
                                 .toCompletableFuture().get(5, SECONDS);

        block.accept(new ServiceFlightAndPassenger(service, flightId, idFromString(response)));
      } catch (Exception e) {
        fail("problem adding passenger - {}", e.getMessage());
      }
    });
  }

  private UUID idFromString(String id) {
    return UUID.fromString(id.split(":")[1]);
  }

  private class ServiceFlightAndPassenger {

    public final FlightService service;

    public final UUID flightId;

    public final UUID passengerId;

    public ServiceFlightAndPassenger(FlightService service, UUID flightId, UUID passengerId) {
      this.service = service;
      this.flightId = flightId;
      this.passengerId = passengerId;
    }
  }
}
