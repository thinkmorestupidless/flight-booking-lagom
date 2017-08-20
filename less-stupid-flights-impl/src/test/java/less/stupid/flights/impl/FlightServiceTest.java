/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package less.stupid.flights.impl;

import less.stupid.flights.api.Flight;
import less.stupid.flights.api.FlightService;
import org.junit.Test;

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

  private void withService(Consumer<FlightService> block) {
    withServer(defaultSetup().withCassandra(true), server -> block.accept(server.client(FlightService.class)));
  }

  private void withServiceAndFlight(BiConsumer<FlightService, String> block) {
    withService(service -> {
      try {
        block.accept(service, service.addFlight()
                                     .invoke(new Flight(callsign, equipment, departure, arrival))
                                     .toCompletableFuture().get(5, SECONDS));
      } catch (Exception e) {
        fail("exception adding flight - {}", e.getMessage());
      }
    });
  }

  @Test
  public void shouldAddFlight() {
    withServiceAndFlight((service, reply) -> assertThat(reply).startsWith("OK"));
  }

  @Test
  public void shouldAddPassenger() {
    withServiceAndFlight((service, flight) -> {

    });
  }
}
