/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package less.stupid.flights.impl;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import less.stupid.flights.api.*;
import less.stupid.flights.api.Passenger;
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
  public ServiceCall<Flight, FlightReply> addFlight() {
    return flight -> {
      AddFlight add = new AddFlight(flight.callsign, flight.equipment, flight.departureIata, flight.arrivalIata);
      return newEntityRef().ask(add);
    };
  }

  @Override
  public ServiceCall<Passenger, Done> addPassenger() {
    return passenger -> {
      AddPassenger add = new AddPassenger(UUID.randomUUID().toString(), passenger.firstName, passenger.lastName, passenger.initial, passenger.seatAssignment);
      return newEntityRef().ask(add);
    };
  }

  @Override
  public ServiceCall<SelectSeat, Done> selectSeat() {
    return seat -> {
      PersistentEntityRef<FlightCommand> ref = persistentEntityRegistry.refFor(FlightEntity.class, seat.flightId.toString());
      return ref.ask(new FlightCommand.SelectSeat(seat.passengerId.toString(), seat.seatAssignment));
    };
  }

  @Override
  public ServiceCall<NotUsed, Done> removePassenger(UUID flightId, UUID passengerId) {
    return request -> {
      PersistentEntityRef<FlightCommand> ref = persistentEntityRegistry.refFor(FlightEntity.class, flightId.toString());
      return ref.ask(new FlightCommand.RemovePassenger(passengerId.toString()));
    };
  }

  @Override
  public ServiceCall<NotUsed, Done> closeFlight(UUID flightId) {
    return request -> {
      PersistentEntityRef<FlightCommand> ref = persistentEntityRegistry.refFor(FlightEntity.class, flightId.toString());
      return ref.ask(new FlightCommand.CloseFlight(flightId.toString()));
    };
  }
}
