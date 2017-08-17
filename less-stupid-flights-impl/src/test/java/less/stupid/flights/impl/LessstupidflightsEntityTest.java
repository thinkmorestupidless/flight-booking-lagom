package less.stupid.flights.impl;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver.Outcome;

import akka.Done;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import less.stupid.flights.impl.FlightCommand.Hello;
import less.stupid.flights.impl.FlightCommand.UseGreetingMessage;
import less.stupid.flights.impl.FlightEvent.GreetingMessageChanged;

public class LessstupidflightsEntityTest {

  static ActorSystem system;

  @BeforeClass
  public static void setup() {
    system = ActorSystem.create("LessstupidflightsEntityTest");
  }

  @AfterClass
  public static void teardown() {
    JavaTestKit.shutdownActorSystem(system);
    system = null;
  }

  @Test
  public void testLessstupidflightsEntity() {
    PersistentEntityTestDriver<FlightCommand, FlightEvent, FlightState> driver = new PersistentEntityTestDriver<>(system,
        new LessstupidflightsEntity(), "world-1");

    Outcome<FlightEvent, FlightState> outcome1 = driver.run(new Hello("Alice"));
    assertEquals("Hello, Alice!", outcome1.getReplies().get(0));
    assertEquals(Collections.emptyList(), outcome1.issues());

    Outcome<FlightEvent, FlightState> outcome2 = driver.run(new UseGreetingMessage("Hi"),
        new Hello("Bob"));
    assertEquals(1, outcome2.events().size());
    assertEquals(new GreetingMessageChanged("world-1", "Hi"), outcome2.events().get(0));
    assertEquals("Hi", outcome2.state().message);
    assertEquals(Done.getInstance(), outcome2.getReplies().get(0));
    assertEquals("Hi, Bob!", outcome2.getReplies().get(1));
    assertEquals(2, outcome2.getReplies().size());
    assertEquals(Collections.emptyList(), outcome2.issues());
  }

}
