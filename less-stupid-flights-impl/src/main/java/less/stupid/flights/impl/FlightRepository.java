package less.stupid.flights.impl;

import akka.Done;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import less.stupid.flights.api.FlightSummary;
import less.stupid.flights.utils.CompletionStageUtils;
import org.pcollections.PSequence;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide.completedStatement;

@Singleton
public class FlightRepository {

    private final CassandraSession session;

    @Inject
    public FlightRepository(CassandraSession session, ReadSide readSide) {
        this.session = session;

        readSide.register(FlightEventProcessor.class);
    }

    public CompletionStage<Set<FlightSummary>> getAllFlights() {
        return session.selectAll(
                "SELECT * FROM activeFlights"
                )
                .thenApply(List::stream)
                .thenApply(rows -> rows.map(FlightRepository::toFlightSummary))
                .thenApply(flightSummaries -> flightSummaries.collect(Collectors.toSet()));
    }

    private static FlightSummary toFlightSummary(Row activeFlight) {
        return new FlightSummary(activeFlight.getUUID("flightId"), activeFlight.getString("callsign"));
    }

    private static class FlightEventProcessor extends ReadSideProcessor<FlightEvent> {

        private final CassandraSession session;
        private final CassandraReadSide readSide;

        private PreparedStatement insertFlightStatement;
        private PreparedStatement deleteFlightStatement;

        @Inject
        public FlightEventProcessor(CassandraSession session, CassandraReadSide readSide) {
            this.session = session;
            this.readSide = readSide;
        }

        @Override
        public ReadSideHandler<FlightEvent> buildHandler() {
            return readSide.<FlightEvent>builder("flightEventOffset")
                           .setGlobalPrepare(this::createTable)
                           .setPrepare(tag -> prepareStatements())
                           .setEventHandler(FlightEvent.FlightAdded.class, e -> insertFlight(UUID.fromString(e.flightId), e.callsign))
                           .setEventHandler(FlightEvent.FlightClosed.class, e -> deleteFlight(UUID.fromString(e.flightId)))
                           .build();
        }

        private CompletionStage<Done> createTable() {
            return session.executeCreateTable(
                    "CREATE TABLE IF NOT EXISTS activeFlights (" +
                            "flightId UUID," +
                            "callSign text," +
                            "PRIMARY KEY (flightId)" +
                          ")"
            );
        }

        private CompletionStage<Done> prepareStatements() {

            return CompletionStageUtils.doAll(

                        session.prepare(
                            "INSERT INTO activeFlights(" +
                             "flightId," +
                             "callSign" +
                            ") VALUES (?, ?)"
                        )
                        .thenAccept(statement -> insertFlightStatement = statement),

                        session.prepare(
                                "DELETE FROM activeFlights" +
                                " WHERE flightId = ?"
                        )
                        .thenAccept(statement -> deleteFlightStatement = statement)
                    );
        }

        private CompletionStage<List<BoundStatement>> insertFlight(UUID flightId, String callsign) {
            return completedStatement(insertFlightStatement.bind(flightId, callsign));
        }

        private CompletionStage<List<BoundStatement>> deleteFlight(UUID flightId) {
            return completedStatement(deleteFlightStatement.bind(flightId));
        }

        @Override
        public PSequence<AggregateEventTag<FlightEvent>> aggregateTags() {
            return FlightEvent.TAG.allTags();
        }
    }
}
