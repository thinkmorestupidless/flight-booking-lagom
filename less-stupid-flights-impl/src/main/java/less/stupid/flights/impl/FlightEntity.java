/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package less.stupid.flights.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import java.util.Collections;
import java.util.Optional;

/**
 * This is an event sourced entity. It has a state, {@link FlightState}
 */
public class FlightEntity extends PersistentEntity<FlightCommand, FlightEvent, FlightState> {

  /**
   * An entity can define different behaviours for different states, but it will
   * always start with an initial behaviour.
   */
  @Override
  public Behavior initialBehavior(Optional<FlightState> snapshotState) {

    BehaviorBuilder b = newBehaviorBuilder(snapshotState.orElse(FlightState.empty()));

    // When a flight is added...
    b.setCommandHandler(FlightCommand.AddFlight.class, (cmd, ctx) ->
      // The flight data is persisted...
      ctx.thenPersist(new FlightEvent.FlightAdded(entityId(), cmd.callsign, cmd.equipment, cmd.departureIata, cmd.arrivalIata),
        // When persist is complete we reply...
        evt -> ctx.reply(String.format("OK:%s", entityId()))));

    // When a flight is completely added...
    // ... we change behaviour now we have a flight available.
    b.setEventHandlerChangingBehavior(FlightEvent.FlightAdded.class,
      evt -> available(new FlightState(Optional.of(new FlightInfo(evt.flightId, evt.callsign, evt.equipment, evt.departureIata, evt.arrivalIata, false)), Collections.emptySet())));

    return b.build();
  }

  private Behavior available(FlightState state) {
    BehaviorBuilder b = newBehaviorBuilder(state);

    // When a passenger is added...
    b.setCommandHandler(FlightCommand.AddPassenger.class, (cmd, ctx) ->
      // The passenger data is persisted...
      ctx.thenPersist(new FlightEvent.PassengerAdded(entityId(), cmd.passengerId, cmd.lastName, cmd.firstName, cmd.initial, cmd.seatAssignment),
        // When persist is complete we reply...
        evt -> ctx.reply(String.format("OK:%s", cmd.passengerId))));

    b.setEventHandler(FlightEvent.PassengerAdded.class,
      evt -> state().withPassenger(new Passenger(evt.passengerId, evt.lastName, evt.firstName, evt.initial, evt.seatAssignment)));


    // When a seat is selected for a passenger...
    b.setCommandHandler(FlightCommand.SelectSeat.class, (cmd, ctx) ->
      ctx.thenPersist(new FlightEvent.SeatSelected(entityId(), cmd.passengerId, cmd.seatAssignment),
        evt -> ctx.reply(Done.getInstance())));

    b.setEventHandler(FlightEvent.SeatSelected.class,
      evt -> {
        Passenger passenger = state().passengers.stream()
                                     .filter(p -> p.passengerId.equals(evt.passengerId))
                                     .findFirst()
                                     .orElseThrow(() -> new RuntimeException(String.format("passenger %s does not exist!", evt.passengerId)))
                                     .withSeatAssignment(Optional.ofNullable(evt.seatAssignment));

        return state().updatePassenger(passenger);
      });

    // When a passenger is flung headlong onto the tarmac (at gunpoint?)
    b.setCommandHandler(FlightCommand.RemovePassenger.class, (cmd, ctx) ->
      ctx.thenPersist(new FlightEvent.PassengerRemoved(entityId(), cmd.passengerId),
        evt -> ctx.reply(Done.getInstance())));

    b.setEventHandler(FlightEvent.PassengerRemoved.class,
      evt -> state().withoutPassenger(evt.passengerId));

    // And, finally, when the flight is closed... all ready to go...
    b.setCommandHandler(FlightCommand.CloseFlight.class, (cmd, ctx) ->
      ctx.thenPersist(new FlightEvent.FlightClosed(entityId()),
        evt -> ctx.reply(Done.getInstance())));

    // Closing the flight moves us into the 'closed' behaviour.
    b.setEventHandlerChangingBehavior(FlightEvent.FlightClosed.class,
      evt -> closed(state().withDoorsClosed(true)));

    return b.build();
  }

  private Behavior closed(FlightState state) {
    BehaviorBuilder b = newBehaviorBuilder(state);

    // When can still march a passenger off the flight after it's closed.
    b.setCommandHandler(FlightCommand.RemovePassenger.class, (cmd, ctx) ->
      ctx.thenPersist(new FlightEvent.PassengerRemoved(entityId(), cmd.passengerId),
        evt -> ctx.reply(Done.getInstance())));

    b.setEventHandler(FlightEvent.PassengerRemoved.class,
      evt -> state().withoutPassenger(evt.passengerId));

    return b.build();
  }
}
