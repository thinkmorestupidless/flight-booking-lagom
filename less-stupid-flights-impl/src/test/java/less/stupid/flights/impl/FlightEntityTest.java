package less.stupid.flights.impl;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver.Outcome;
import org.junit.*;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class FlightEntityTest {

  private static ActorSystem system;

  private PersistentEntityTestDriver<FlightCommand, FlightEvent, FlightState> driver;

  private static final UUID flightId          = UUID.randomUUID();
  private static final String DateTimePattern = "yyyy-MM-dd HH:mm:ss";
  private static final String callsign        = "UA100";
  private static final String equipment       = "757-800";
  private static final String departure       = "EWR";
  private static final String arrival         = "SFO";

  private static final Passenger passenger1   = new Passenger(UUID.randomUUID().toString(), "Walsh", "Sean", "A", Optional.of("1A"));
  private static final Passenger passenger2   = new Passenger(UUID.randomUUID().toString(), "Smith", "John", "P", Optional.empty());


  @BeforeClass
  public static void setup() {
    system = ActorSystem.create("FlightEntityTest");
  }

  @AfterClass
  public static void teardown() {
    JavaTestKit.shutdownActorSystem(system);
    system = null;
  }

  @Before
  public void createTestDriver() {
    driver = new PersistentEntityTestDriver<>(system, new FlightEntity(), flightId.toString());
  }

  @After
  public void noIssues() {
    if (!driver.getAllIssues().isEmpty()) {
      driver.getAllIssues().forEach(System.out::println);
      fail("There were issues " + driver.getAllIssues().get(0));
    }
  }

  private void withDriverAndFlight(BiConsumer<PersistentEntityTestDriver<FlightCommand, FlightEvent, FlightState>, Outcome<FlightEvent, FlightState>> block) {
    block.accept(driver, driver.run(new FlightCommand.AddFlight(callsign, equipment, departure, arrival)));
  }

  @Test
  public void shouldCreateNewFlight() {
    withDriverAndFlight((driver, flight) -> {
      assertThat(flight.events()).containsOnly(new FlightEvent.FlightAdded(flightId.toString(), callsign, equipment, departure, arrival));
      assertThat(flight.state()).isEqualTo(new FlightState(Optional.of(new FlightInfo(flightId.toString(), callsign, equipment, departure, arrival, false)), Collections.emptySet()));
    });
  }

  @Test
  public void shouldAddPassengerWithSeatAssignment() {
    withDriverAndFlight((driver, flight) -> {
      Outcome<FlightEvent, FlightState> outcome = driver.run(new FlightCommand.AddPassenger(passenger1.passengerId, passenger1.lastName, passenger1.firstName, passenger1.initial, passenger1.seatAssignment));
      assertThat(outcome.events()).containsOnly(new FlightEvent.PassengerAdded(flightId.toString(), passenger1.passengerId, passenger1.lastName, passenger1.firstName, passenger1.initial, passenger1.seatAssignment));
      assertThat(outcome.state()).isEqualTo(new FlightState(Optional.of(new FlightInfo(flightId.toString(), callsign, equipment, departure, arrival, false)), Collections.singleton(passenger1)));
    });
  }

  @Test
  public void shouldAddPassengerWithoutSeatAssignment() {
    withDriverAndFlight((driver, flight) -> {
      Outcome<FlightEvent, FlightState> outcome = driver.run(new FlightCommand.AddPassenger(passenger2.passengerId, passenger2.lastName, passenger2.firstName, passenger2.initial, passenger2.seatAssignment));
      assertThat(outcome.events()).containsOnly(new FlightEvent.PassengerAdded(flightId.toString(), passenger2.passengerId, passenger2.lastName, passenger2.firstName, passenger2.initial, passenger2.seatAssignment));
      assertThat(outcome.state()).isEqualTo(new FlightState(Optional.of(new FlightInfo(flightId.toString(), callsign, equipment, departure, arrival, false)), Collections.singleton(passenger2)));
    });
  }

  @Test
  public void shouldSelectSeat() {
    withDriverAndFlight((driver, flight) -> {
      String seatAssignment = "1B";

      Outcome<FlightEvent, FlightState> outcome = driver.run(new FlightCommand.AddPassenger(passenger2.passengerId, passenger2.lastName, passenger2.firstName, passenger2.initial, passenger2.seatAssignment));
      assertThat(outcome.events()).containsOnly(new FlightEvent.PassengerAdded(flightId.toString(), passenger2.passengerId, passenger2.lastName, passenger2.firstName, passenger2.initial, passenger2.seatAssignment));
      assertThat(outcome.state()).isEqualTo(new FlightState(Optional.of(new FlightInfo(flightId.toString(), callsign, equipment, departure, arrival, false)), Collections.singleton(passenger2)));

      Outcome<FlightEvent, FlightState> outcome2 = driver.run(new FlightCommand.SelectSeat(passenger2.passengerId, seatAssignment));
      assertThat(outcome2.events()).containsOnly(new FlightEvent.SeatSelected(flightId.toString(), passenger2.passengerId, seatAssignment));
      assertThat(outcome2.state()).isEqualTo(new FlightState(Optional.of(new FlightInfo(flightId.toString(), callsign, equipment, departure, arrival, false)), Collections.singleton(passenger2.withSeatAssignment(Optional.of(seatAssignment)))));
    });
  }

  @Test
  public void shouldSelectNewSeat() {
    withDriverAndFlight((driver, flight) -> {
      String seatAssignment = "1B";

      Outcome<FlightEvent, FlightState> outcome = driver.run(new FlightCommand.AddPassenger(passenger1.passengerId, passenger1.lastName, passenger1.firstName, passenger1.initial, passenger1.seatAssignment));
      assertThat(outcome.events()).containsOnly(new FlightEvent.PassengerAdded(flightId.toString(), passenger1.passengerId, passenger1.lastName, passenger1.firstName, passenger1.initial, passenger1.seatAssignment));
      assertThat(outcome.state()).isEqualTo(new FlightState(Optional.of(new FlightInfo(flightId.toString(), callsign, equipment, departure, arrival, false)), Collections.singleton(passenger1)));

      Outcome<FlightEvent, FlightState> outcome2 = driver.run(new FlightCommand.SelectSeat(passenger1.passengerId, seatAssignment));
      assertThat(outcome2.events()).containsOnly(new FlightEvent.SeatSelected(flightId.toString(), passenger1.passengerId, seatAssignment));
      assertThat(outcome2.state()).isEqualTo(new FlightState(Optional.of(new FlightInfo(flightId.toString(), callsign, equipment, departure, arrival, false)), Collections.singleton(passenger1.withSeatAssignment(Optional.of(seatAssignment)))));
    });
  }

  @Test
  public void shouldRemovePassengerFromOpenFlight() {
    withDriverAndFlight((driver, flight) -> {
      Outcome<FlightEvent, FlightState> outcome = driver.run(new FlightCommand.AddPassenger(passenger1.passengerId, passenger1.lastName, passenger1.firstName, passenger1.initial, passenger1.seatAssignment));
      assertThat(outcome.events()).containsOnly(new FlightEvent.PassengerAdded(flightId.toString(), passenger1.passengerId, passenger1.lastName, passenger1.firstName, passenger1.initial, passenger1.seatAssignment));
      assertThat(outcome.state()).isEqualTo(new FlightState(Optional.of(new FlightInfo(flightId.toString(), callsign, equipment, departure, arrival, false)), Collections.singleton(passenger1)));

      Outcome<FlightEvent, FlightState> outcome2 = driver.run(new FlightCommand.RemovePassenger(passenger1.passengerId));
      assertThat(outcome2.events()).containsOnly(new FlightEvent.PassengerRemoved(flightId.toString(), passenger1.passengerId));
      assertThat(outcome2.state()).isEqualTo(new FlightState(Optional.of(new FlightInfo(flightId.toString(), callsign, equipment, departure, arrival, false)), Collections.emptySet()));
    });
  }

  @Test
  public void shouldCloseFlight() {
    withDriverAndFlight((driver, flight) -> {
      Outcome<FlightEvent, FlightState> outcome = driver.run(new FlightCommand.CloseFlight(flightId.toString()));
      assertThat(outcome.events()).containsOnly(new FlightEvent.FlightClosed(flightId.toString()));
      assertThat(outcome.state()).isEqualTo(new FlightState(Optional.of(new FlightInfo(flightId.toString(), callsign, equipment, departure, arrival, true)), Collections.emptySet()));
    });
  }

  @Test
  public void shouldRemovePassengerFromClosedFlight() {
    withDriverAndFlight((driver, flight) -> {
      Outcome<FlightEvent, FlightState> outcome = driver.run(new FlightCommand.AddPassenger(passenger1.passengerId, passenger1.lastName, passenger1.firstName, passenger1.initial, passenger1.seatAssignment));
      assertThat(outcome.events()).containsOnly(new FlightEvent.PassengerAdded(flightId.toString(), passenger1.passengerId, passenger1.lastName, passenger1.firstName, passenger1.initial, passenger1.seatAssignment));
      assertThat(outcome.state()).isEqualTo(new FlightState(Optional.of(new FlightInfo(flightId.toString(), callsign, equipment, departure, arrival, false)), Collections.singleton(passenger1)));

      Outcome<FlightEvent, FlightState> outcome2 = driver.run(new FlightCommand.CloseFlight(flightId.toString()));
      assertThat(outcome2.events()).containsOnly(new FlightEvent.FlightClosed(flightId.toString()));
      assertThat(outcome2.state()).isEqualTo(new FlightState(Optional.of(new FlightInfo(flightId.toString(), callsign, equipment, departure, arrival, true)), Collections.singleton(passenger1)));

      Outcome<FlightEvent, FlightState> outcome3 = driver.run(new FlightCommand.RemovePassenger(passenger1.passengerId));
      assertThat(outcome3.events()).containsOnly(new FlightEvent.PassengerRemoved(flightId.toString(), passenger1.passengerId));
      assertThat(outcome3.state()).isEqualTo(new FlightState(Optional.of(new FlightInfo(flightId.toString(), callsign, equipment, departure, arrival, true)), Collections.emptySet()));
    });
  }
}
