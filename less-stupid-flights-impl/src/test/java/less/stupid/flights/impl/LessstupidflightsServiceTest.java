/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package less.stupid.flights.impl;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import less.stupid.flights.api.GreetingMessage;
import less.stupid.flights.api.LessstupidflightsService;

public class LessstupidflightsServiceTest {

  @Test
  public void shouldStorePersonalizedGreeting() throws Exception {
    withServer(defaultSetup().withCassandra(true), server -> {
      LessstupidflightsService service = server.client(LessstupidflightsService.class);

      String msg1 = service.hello("Alice").invoke().toCompletableFuture().get(5, SECONDS);
      assertEquals("Hello, Alice!", msg1); // default greeting

      service.useGreeting("Alice").invoke(new GreetingMessage("Hi")).toCompletableFuture().get(5, SECONDS);
      String msg2 = service.hello("Alice").invoke().toCompletableFuture().get(5, SECONDS);
      assertEquals("Hi, Alice!", msg2);

      String msg3 = service.hello("Bob").invoke().toCompletableFuture().get(5, SECONDS);
      assertEquals("Hello, Bob!", msg3); // default greeting
    });
  }

}
