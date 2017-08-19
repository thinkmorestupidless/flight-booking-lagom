package less.stupid.flights.impl;

import static org.assertj.core.api.Assertions.assertThat;

import akka.Done;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.github.javafaker.Faker;
import less.stupid.flights.api.FlightReply;

import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver.Outcome;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;
import java.util.UUID;

public class FlightEntityTest {

  static ActorSystem system;

  static Faker faker = new Faker();

  @BeforeClass
  public static void setup() {
    system = ActorSystem.create("FlightEntityTest");
  }

  @AfterClass
  public static void teardown() {
    JavaTestKit.shutdownActorSystem(system);
    system = null;
  }

  /**
   * Here we test the 'happy path' - everything as it should be.
   *
   * AddFlight
   * AddPassenger
   * SelectSeat
   * RemovePassenger
   * CloseFlight
   *
   */
//  @Test
  public void happyFace() {
    PersistentEntityTestDriver<FlightCommand, FlightEvent, FlightState> driver = new PersistentEntityTestDriver<>(system, new FlightEntity(), "world-1");

    Outcome<FlightEvent, FlightState> addFlight = driver.run(new FlightCommand.AddFlight("a", "b", "c", "d"));

    assertThat(addFlight.getReplies()).hasSize(1);
    assertThat(addFlight.getReplies().get(0)).isInstanceOf(FlightReply.class);
    
    FlightReply reply1 = (FlightReply) addFlight.getReplies().get(0);
    String flightId = reply1.flightId;
    assertThat(flightId).isNotEmpty();

    String passengerId = UUID.randomUUID().toString();

    Outcome<FlightEvent, FlightState> addPassenger = driver.run(new FlightCommand.AddPassenger(passengerId, faker.name().lastName(), faker.name().firstName(), "i", Optional.of(faker.address().state())));

    assertThat(addPassenger.getReplies()).hasSize(1);
    assertThat(addPassenger.getReplies().get(0)).isInstanceOf(Done.class);

    Done reply2 = (Done) addPassenger.getReplies().get(0);
    assertThat(reply2).isNotNull();

    String seatAssignment = "ABC";

    Outcome<FlightEvent, FlightState> selectSeat = driver.run(new FlightCommand.SelectSeat(passengerId, seatAssignment));

    assertThat(selectSeat.getReplies()).hasSize(1);
    assertThat(selectSeat.getReplies().get(0)).isInstanceOf(Done.class);

    Outcome<FlightEvent, FlightState> removePassenger = driver.run(new FlightCommand.RemovePassenger(passengerId));

    assertThat(removePassenger.getReplies()).hasSize(1);
    assertThat(removePassenger.getReplies().get(0)).isInstanceOf(Done.class);

    Outcome<FlightEvent, FlightState> closeFlight = driver.run(new FlightCommand.CloseFlight(flightId));
  }

}
