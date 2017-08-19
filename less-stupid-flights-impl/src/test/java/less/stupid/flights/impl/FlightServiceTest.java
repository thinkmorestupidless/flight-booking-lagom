/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package less.stupid.flights.impl;

import less.stupid.flights.api.FlightService;
import org.junit.Test;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;

public class FlightServiceTest {

//  @Test
  public void shouldStorePersonalizedGreeting() throws Exception {
    withServer(defaultSetup().withCassandra(true), server -> {
      FlightService service = server.client(FlightService.class);

//      String msg1 = service.hello("Alice").invoke().toCompletableFuture().get(5, SECONDS);
//      assertEquals("Hello, Alice!", msg1); // default greeting
//
//      service.useGreeting("Alice").invoke(new GreetingMessage("Hi")).toCompletableFuture().get(5, SECONDS);
//      String msg2 = service.hello("Alice").invoke().toCompletableFuture().get(5, SECONDS);
//      assertEquals("Hi, Alice!", msg2);
//
//      String msg3 = service.hello("Bob").invoke().toCompletableFuture().get(5, SECONDS);
//      assertEquals("Hello, Bob!", msg3); // default greeting
    });
  }

}
