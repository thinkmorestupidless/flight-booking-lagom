/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package less.stupid.flights.impl;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import less.stupid.flights.api.Flight;
import less.stupid.flights.api.FlightService;
import less.stupid.flights.api.Passenger;
import less.stupid.flights.api.SelectSeat;
import less.stupid.flights.impl.FlightCommand.AddFlight;
import less.stupid.flights.impl.FlightCommand.AddPassenger;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Implementation of the LessstupidflightsService.
 */
public class FlightServiceImpl implements FlightService {

  private final PersistentEntityRegistry persistentEntityRegistry;

  @Inject
  public FlightServiceImpl(PersistentEntityRegistry persistentEntityRegistry) {
    this.persistentEntityRegistry = persistentEntityRegistry;
    persistentEntityRegistry.register(FlightEntity.class);
  }

  private PersistentEntityRef<FlightCommand> newEntityRef() {
    return persistentEntityRegistry.refFor(FlightEntity.class, UUID.randomUUID().toString());
  }

  @Override
  public ServiceCall<Flight, String> addFlight() {
    return flight -> {
      AddFlight add = new AddFlight(flight.callsign, flight.equipment, flight.departureIata, flight.arrivalIata);
      return newEntityRef().ask(add);
    };
  }

  @Override
  public ServiceCall<Passenger, String> addPassenger() {
    return passenger -> {
      PersistentEntityRef<FlightCommand> ref = persistentEntityRegistry.refFor(FlightEntity.class, passenger.flightId.toString());
      return ref.ask(new AddPassenger(UUID.randomUUID().toString(), passenger.firstName, passenger.lastName, passenger.initial, passenger.seatAssignment));
    };
  }

  @Override
  public ServiceCall<SelectSeat, String> selectSeat() {
    return seat -> {
      PersistentEntityRef<FlightCommand> ref = persistentEntityRegistry.refFor(FlightEntity.class, seat.flightId.toString());
      return ref.ask(new FlightCommand.SelectSeat(seat.passengerId.toString(), seat.seatAssignment)).thenApply($ -> "OK");
    };
  }

  @Override
  public ServiceCall<NotUsed, String> removePassenger(UUID flightId, UUID passengerId) {
    return request -> {
      PersistentEntityRef<FlightCommand> ref = persistentEntityRegistry.refFor(FlightEntity.class, flightId.toString());
      return ref.ask(new FlightCommand.RemovePassenger(passengerId.toString())).thenApply($ -> "OK");
    };
  }

  @Override
  public ServiceCall<NotUsed, String> closeFlight(UUID flightId) {
    return request -> {
      PersistentEntityRef<FlightCommand> ref = persistentEntityRegistry.refFor(FlightEntity.class, flightId.toString());
      return ref.ask(new FlightCommand.CloseFlight(flightId.toString())).thenApply($ -> "OK");
    };
  }
}
