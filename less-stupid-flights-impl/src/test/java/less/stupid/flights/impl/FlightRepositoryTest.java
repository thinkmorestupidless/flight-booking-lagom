package less.stupid.flights.impl;

import com.lightbend.lagom.internal.javadsl.api.broker.TopicFactory;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import less.stupid.flights.api.FlightSummary;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.bind;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static org.assertj.core.api.Assertions.assertThat;

public class FlightRepositoryTest {

    private static final String equipment       = "757-800";
    private static final String departure       = "EWR";
    private static final String arrival         = "SFO";

    private final static ServiceTest.Setup setup = defaultSetup().withCassandra(true)
            .configureBuilder(b ->
                    // by default, cassandra-query-journal delays propagation of events by 10sec. In test we're using
                    // a 1 node cluster so this delay is not necessary.
                    b.configure("cassandra-query-journal.eventual-consistency-delay", "0")
                            .overrides(bind(ReadSide.class).to(ReadSideTestDriver.class),
                                    bind(TopicFactory.class).to(DoNothingTopicFactory.class))
            );

    private static ServiceTest.TestServer testServer;

    @BeforeClass
    public static void beforeAll() {
        testServer = ServiceTest.startServer(setup);
    }

    @AfterClass
    public static void afterAll() {
        testServer.stop();
    }

    private ReadSideTestDriver testDriver = testServer.injector().instanceOf(ReadSideTestDriver.class);
    private FlightRepository flightRepository = testServer.injector().instanceOf(FlightRepository.class);
    private AtomicInteger offset;

    @Before
    public void restartOffset() {
        offset = new AtomicInteger(1);
    }

    @Test
    public void shouldListAllActiveFlights() throws InterruptedException, ExecutionException, TimeoutException {
        UUID flight1 = UUID.randomUUID();
        UUID flight2 = UUID.randomUUID();
        UUID flight3 = UUID.randomUUID();

        feed(new FlightEvent.FlightAdded(flight1.toString(), "UA100", equipment, departure, arrival));
        feed(new FlightEvent.FlightAdded(flight2.toString(), "UA101", equipment, departure, arrival));
        feed(new FlightEvent.FlightAdded(flight3.toString(), "UA202", equipment, departure, arrival));

        Set<FlightSummary> flights = Await.result(flightRepository.getAllFlights());

        assertThat(flights).hasSize(3).containsOnly(new FlightSummary(flight1, "UA100"), new FlightSummary(flight2, "UA101"), new FlightSummary(flight3, "UA202"));
    }

    private void feed(FlightEvent flightEvent) throws InterruptedException, ExecutionException, TimeoutException {
        Await.result(testDriver.feed(flightEvent, Offset.sequence(offset.getAndIncrement())));
    }
}
