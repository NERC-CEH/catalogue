package uk.ac.ceh.gateway.catalogue.exports;

import lombok.val;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * The pipeline the phase 4 mappings share.
 *
 * <p>Driven through a stub source rather than the real four, so that what is
 * being tested is the pipeline and not any authority's data. The mappings
 * themselves are covered by {@link ReferenceSourceTest} against real responses.
 */
@DisplayName("Asking a phase 4 authority (dri-one #350)")
class ReferenceRetrieverTest {

    private static final String IRI = "https://example.invalid/thing/1";

    private Dataset dataset;
    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private DescriptionCache cache;

    @BeforeEach
    void setUp() {
        dataset = TDB2Factory.createDataset();
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        cache = new DescriptionCache(dataset, Clock.systemUTC());
    }

    @AfterEach
    void tearDown() {
        dataset.close();
    }

    /** A source with no data of its own: it labels whatever it is asked about. */
    private static class StubSource implements ReferenceSource {
        private final int budget;

        StubSource(int budget) {
            this.budget = budget;
        }

        @Override
        public String graph() {
            return "https://example.invalid/";
        }

        @Override
        public String title() {
            return "A stand-in authority";
        }

        @Override
        public boolean describes(String iri) {
            return iri.startsWith("https://example.invalid/thing/");
        }

        @Override
        public String requestUrl(String iri) {
            return iri + "/about";
        }

        @Override
        public String accept() {
            return "application/json";
        }

        @Override
        public Model describe(String iri, String body) {
            val model = ModelFactory.createDefaultModel();
            if (body.contains("nothing")) {
                return model;
            }
            model.add(model.getResource(iri), RDFS.label, body.trim());
            return model;
        }

        @Override
        public Duration maxAge() {
            return Duration.ofDays(30);
        }

        @Override
        public int requestsPerRun() {
            return budget;
        }
    }


    /** A source whose request URL carries percent-encoding, as GtR's does. */
    private static class EncodedSource extends StubSource {
        EncodedSource() {
            super(10);
        }

        @Override
        public String requestUrl(String iri) {
            return "https://example.invalid/api?q=NE%2FR016429%2F1";
        }
    }

    private ReferenceRetriever retriever(ReferenceSource source) {
        return new ReferenceRetriever(restTemplate, cache, List.of(source));
    }

    private static List<String> things(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> "https://example.invalid/thing/%02d".formatted(i))
            .toList();
    }

    @Nested
    @DisplayName("Asking, and remembering the answer")
    class Fetching {

        @Test
        @DisplayName("the source decides the URL, which need not be the IRI")
        void requestUrlComesFromTheSource() {
            val source = new StubSource(10);
            server.expect(requestTo(IRI + "/about"))
                .andExpect(header("Accept", "application/json"))
                .andRespond(withSuccess("a thing", MediaType.APPLICATION_JSON));

            val described = retriever(source).describe(List.of(IRI), source);

            server.verify();
            assertTrue(described.model().contains(createResource(IRI), RDFS.label, "a thing"));
        }

        @Test
        @DisplayName("a percent-encoded url is sent as it was built, not re-encoded")
        void percentEncodingSurvives() {
            // RestTemplate treats a String url as a URI template and encodes it
            // again, so %2F becomes %252F. GtR grant references contain slashes
            // and are encoded for its search parameter, so this silently
            // returned zero descriptions for all 259 of them -- a 200 every
            // time, and nothing in any log to say why.
            val source = new EncodedSource();
            server.expect(requestTo("https://example.invalid/api?q=NE%2FR016429%2F1"))
                .andRespond(withSuccess("a grant", MediaType.APPLICATION_JSON));

            retriever(source).describe(List.of("https://example.invalid/thing/1"), source);

            // The expectation is the assertion: a double-encoded %252F does not
            // match it.
            server.verify();
        }

        @Test
        @DisplayName("a second run takes the held copy instead of asking again")
        void secondRunUsesTheCache() {
            val source = new StubSource(10);
            server.expect(once(), requestTo(IRI + "/about"))
                .andRespond(withSuccess("a thing", MediaType.APPLICATION_JSON));

            val retriever = retriever(source);
            retriever.describe(List.of(IRI), source);
            val second = retriever.describe(List.of(IRI), source);

            server.verify();
            assertTrue(second.model().contains(createResource(IRI), RDFS.label, "a thing"));
        }

        @Test
        @DisplayName("an authority that has nothing to say is not asked again every run")
        void nothingToSayIsRemembered() {
            val source = new StubSource(10);
            server.expect(once(), requestTo(IRI + "/about"))
                .andRespond(withSuccess("nothing", MediaType.APPLICATION_JSON));

            val retriever = retriever(source);
            retriever.describe(List.of(IRI), source);
            val second = retriever.describe(List.of(IRI), source);

            // once() means a second request fails verification. 61 sites and 882
            // works is enough that re-asking the ones with no answer would be a
            // daily waste on both sides.
            server.verify();
            assertThat(second.model().size(), is(0L));
        }
    }

    @Nested
    @DisplayName("When the authority pushes back")
    class PushBack {

        @Test
        @DisplayName("a 429 ends the run's dealings with it")
        void rateLimitStopsTheRun() {
            val source = new StubSource(50);
            server.expect(once(), requestTo(org.hamcrest.Matchers.startsWith("https://example.invalid/")))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

            val described = retriever(source).describe(things(20), source);

            server.verify();
            assertThat("the rest are left for tomorrow", described.deferred(), is(19));
            assertThat(described.isComplete(), is(false));
        }

        @Test
        @DisplayName("a failed request costs budget, so a failing authority is not hammered")
        void failuresConsumeBudget() {
            val source = new StubSource(5);
            IntStream.range(0, 5).forEach(i ->
                server.expect(requestTo(org.hamcrest.Matchers.startsWith("https://example.invalid/")))
                    .andRespond(withServerError()));

            retriever(source).describe(things(20), source);

            // Five expectations for twenty things: a sixth request would throw.
            server.verify();
        }

        @Test
        @DisplayName("a 5xx holds the graph back; a 404 does not")
        void transientAndDefinitiveAreDistinguished() {
            val source = new StubSource(10);
            server.expect(requestTo(IRI + "/about")).andRespond(withServerError());
            assertThat(retriever(source).describe(List.of(IRI), source).transientFailures(), is(1));

            val other = "https://example.invalid/thing/2";
            val second = MockRestServiceServer.createServer(restTemplate);
            second.expect(requestTo(other + "/about")).andRespond(withStatus(HttpStatus.NOT_FOUND));
            val described = new ReferenceRetriever(restTemplate, cache, List.of(source))
                .describe(List.of(other), source);

            assertThat("a 404 would otherwise hold the graph back for ever",
                described.transientFailures(), is(0));
            assertThat(described.isComplete(), is(true));
        }

        @Test
        @DisplayName("a copy of any age beats losing the entity from the graph")
        void staleBeatsNothing() {
            val source = new StubSource(10);
            server.expect(requestTo(IRI + "/about"))
                .andRespond(withSuccess("a thing", MediaType.APPLICATION_JSON));
            retriever(source).describe(List.of(IRI), source);

            val later = MockRestServiceServer.createServer(restTemplate);
            later.expect(requestTo(IRI + "/about")).andRespond(withServerError());
            val aged = new DescriptionCache(dataset,
                Clock.fixed(java.time.Instant.now().plus(Duration.ofDays(100)),
                    java.time.ZoneOffset.UTC));

            val described = new ReferenceRetriever(restTemplate, aged, List.of(source))
                .describe(List.of(IRI), source);

            assertTrue(described.model().contains(createResource(IRI), RDFS.label, "a thing"));
            assertThat("nothing is missing from the graph, so it may still publish",
                described.isComplete(), is(true));
        }
    }
}
