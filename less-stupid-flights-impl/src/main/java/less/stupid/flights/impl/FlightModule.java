/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package less.stupid.flights.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import less.stupid.flights.api.FlightService;

/**
 * The module that binds the FlightService so that it can be served.
 */
public class FlightModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    bindService(FlightService.class, FlightServiceImpl.class);
  }
}
