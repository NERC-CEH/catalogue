package uk.ac.ceh.gateway.catalogue.exports;

import lombok.val;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * The policy every authority shares, in one place instead of four near-copies.
 *
 * <p>Three of these assertions are the reasons the loop is shaped as it is, and
 * each was learned the hard way: a budget that counted successes stopped
 * limiting a failing authority, which is the one condition it exists for; a 429
 * answered by continuing to the ceiling is both rude and pointless; and a
 * transient failure treated like a definitive one lets a mistyped identifier
 * hold a graph back for ever. They were maintained in four files by comment and
 * vigilance; they are now maintained by name.
 */
@DisplayName("Asking an authority about the entities the catalogue cites")
class AuthorityRetrieverTest {

    private static final String GRAPH = "https://example.invalid/";
    private static final String THING = GRAPH + "thing/";
    private static final Instant NOW = Instant.parse("2026-09-07T10:00:00Z");

    private Dataset dataset;
    private DescriptionCache cache;
    private MockRestServiceServer server;
    private AuthorityRetriever retriever;

    @BeforeEach
    void setUp() {
        dataset = DatasetFactory.create();
        cache = new DescriptionCache(dataset, Clock.fixed(NOW, ZoneOffset.UTC));
        val restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        retriever = new AuthorityRetriever(restTemplate, cache);
    }

    @AfterEach
    void tearDown() {
        dataset.close();
    }

    /** A source that labels whatever it is asked about. */
    private static class StubSource implements AuthoritySource {
        private final int budget;
        private final int batch;
        final List<List<String>> asked = new ArrayList<>();

        StubSource(int budget, int batch) {
            this.budget = budget;
            this.batch = batch;
        }

        @Override public String graph() {
            return GRAPH;
        }

        @Override public String title() {
            return "A stand-in authority";
        }

        @Override public String description() {
            return "Whatever it is asked about, labelled.";
        }

        @Override public List<String> vocabularies() {
            return List.of(RDFS.getURI());
        }

        @Override public boolean describes(String iri) {
            return iri.startsWith(THING);
        }

        @Override public Duration maxAge() {
            return Duration.ofDays(7);
        }

        @Override public int requestsPerRun() {
            return budget;
        }

        @Override public int batchSize() {
            return batch;
        }

        @Override
        public Request request(List<String> batch) {
            asked.add(List.copyOf(batch));
            return Request.get(GRAPH + "about?ids=" + batch.size(), "text/turtle");
        }

        @Override
        public Map<String, Model> describe(List<String> batch, String body) {
            val byIri = new LinkedHashMap<String, Model>();
            for (val iri : batch) {
                val model = ModelFactory.createDefaultModel();
                model.add(model.getResource(iri), RDFS.label, body.trim());
                byIri.put(iri, model);
            }
            return byIri;
        }
    }

    private void respondWith(String body) {
        server.expect(requestTo(startsWith(GRAPH + "about")))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(body, MediaType.valueOf("text/turtle")));
    }

    private void respondWith(HttpStatus status) {
        server.expect(requestTo(startsWith(GRAPH + "about"))).andRespond(withStatus(status));
    }

    private static String labelOf(Model model, String iri) {
        val statements = model
            .listStatements(model.getResource(iri), RDFS.label, (RDFNode) null).toList();
        return statements.isEmpty() ? null : statements.getFirst().getObject().asLiteral().getString();
    }

    @Nested
    @DisplayName("The budget")
    class Budget {

        @Test
        @DisplayName("counts attempts, so a failing authority is still limited")
        void countsAttemptsNotSuccesses() {
            val source = new StubSource(2, 1);
            respondWith(HttpStatus.INTERNAL_SERVER_ERROR);
            respondWith(HttpStatus.INTERNAL_SERVER_ERROR);

            val described = retriever.describe(
                List.of(THING + "a", THING + "b", THING + "c"), source);

            assertThat("a budget that counted successes would have asked about all three",
                source.asked.size(), is(2));
            assertThat("the third was never reached", described.deferred(), is(1));
            assertThat("the two that were asked about failed transiently",
                described.transientFailures(), is(2));
        }

        @Test
        @DisplayName("is spent per request, not per entity, once a source batches")
        void countsRequestsWhenBatching() {
            val source = new StubSource(1, 2);
            respondWith("first");

            val described = retriever.describe(
                List.of(THING + "a", THING + "b", THING + "c"), source);

            assertThat("one request covered two entities",
                source.asked, contains(List.of(THING + "a", THING + "b")));
            assertThat("the third entity is left for the next run", described.deferred(), is(1));
        }
    }

    @Nested
    @DisplayName("When an authority asks us to slow down")
    class RateLimited {

        @Test
        @DisplayName("nothing more is asked of it this run")
        void stopsForTheRest() {
            val source = new StubSource(10, 1);
            respondWith(HttpStatus.TOO_MANY_REQUESTS);

            val described = retriever.describe(
                List.of(THING + "a", THING + "b", THING + "c"), source);

            assertThat("continuing to the budget ceiling would be rude and pointless",
                source.asked.size(), is(1));
            assertThat(described.transientFailures(), is(1));
            assertThat(described.deferred(), is(2));
        }
    }

    @Nested
    @DisplayName("Classifying a failure")
    class Failures {

        @Test
        @DisplayName("a 404 is definitive, so a mistyped identifier cannot hold a graph back")
        void notFoundIsDefinitive() {
            val source = new StubSource(10, 1);
            respondWith(HttpStatus.NOT_FOUND);

            val described = retriever.describe(List.of(THING + "a"), source);

            assertThat("no later run can help a 404, so it must not block publishing",
                described.isComplete(), is(true));
            assertThat(described.transientFailures(), is(0));
            assertThat(described.deferred(), is(0));
        }

        @Test
        @DisplayName("a 403 is transient, so it holds the graph back and gets noticed")
        void forbiddenIsTransient() {
            val source = new StubSource(10, 1);
            respondWith(HttpStatus.FORBIDDEN);

            val described = retriever.describe(List.of(THING + "a"), source);

            assertThat("our fault or a misconfiguration: better noticed than published around",
                described.isComplete(), is(false));
            assertThat(described.transientFailures(), is(1));
        }

        @Test
        @DisplayName("a 200 with an empty body is transient, not an entity that does not exist")
        void emptyBodyIsTransient() {
            val source = new StubSource(10, 1);
            respondWith("");

            val described = retriever.describe(List.of(THING + "a"), source);

            assertThat(described.transientFailures(), is(1));
        }

        @Test
        @DisplayName("a body the mapper cannot read is transient, not the authority saying nothing")
        void unreadableBodyIsTransient() {
            // The distinction is load-bearing, and preserving it was the reason
            // AuthorityRetriever catches from describe at all. ORCID and ROR
            // classified an unreadable response as transient -- an error page
            // served with a 200 is not the authority saying it holds nothing --
            // whereas the phase 4 mappers swallow it and return empty, for
            // records that are odd rather than absent. Both survive: throwing
            // means transient, returning empty means definitive.
            val source = new StubSource(10, 1) {
                @Override
                public Map<String, Model> describe(List<String> batch, String body) {
                    throw new IllegalStateException("not the RDF we asked for");
                }
            };
            respondWith("<html>Service Unavailable</html>");

            val described = retriever.describe(List.of(THING + "a"), source);

            assertThat("must hold the graph back and be tried again",
                described.transientFailures(), is(1));
            assertThat("and must not be remembered as a negative",
                cache.get(THING + "a", Duration.ofDays(7)).isPresent(), is(false));
        }

        @Test
        @DisplayName("a request that cannot even be built is transient, not a crash")
        void unbuildableRequestIsTransient() {
            val source = new StubSource(10, 1) {
                @Override
                public Request request(List<String> batch) {
                    return Request.get("https://example.invalid/a space", "text/turtle");
                }
            };

            val described = retriever.describe(List.of(THING + "a"), source);

            assertThat("a bad URI built from an authority's own data must not escape the loop",
                described.transientFailures(), is(1));
        }
    }

    @Nested
    @DisplayName("The cache")
    class Cache {

        @Test
        @DisplayName("a fresh copy is used without asking the authority at all")
        void freshCopyIsNotRefetched() {
            val source = new StubSource(10, 1);
            val held = ModelFactory.createDefaultModel();
            held.add(held.getResource(THING + "a"), RDFS.label, "from the cache");
            cache.put(THING + "a", held);

            val described = retriever.describe(List.of(THING + "a"), source);

            assertThat(source.asked, is(List.of()));
            assertThat(labelOf(described.model(), THING + "a"), is("from the cache"));
        }

        @Test
        @DisplayName("a copy of any age beats nothing when the authority cannot be reached")
        void staleCopyBeatsNothing() {
            val source = new StubSource(10, 1);
            val stale = ModelFactory.createDefaultModel();
            stale.add(stale.getResource(THING + "a"), RDFS.label, "a fortnight old");
            // Stored against a clock two years back, so it is well past maxAge.
            new DescriptionCache(dataset, Clock.fixed(Instant.parse("2024-09-07T10:00:00Z"), ZoneOffset.UTC))
                .put(THING + "a", stale);
            respondWith(HttpStatus.INTERNAL_SERVER_ERROR);

            val described = retriever.describe(List.of(THING + "a"), source);

            assertThat("a name from a fortnight ago is still that entity's name",
                labelOf(described.model(), THING + "a"), is("a fortnight old"));
            assertThat("and it is not a failure the caller must hold the graph for",
                described.transientFailures(), is(0));
        }

        @Test
        @DisplayName("an authority reached with nothing to say is remembered, not asked again")
        void negativeIsCached() {
            val source = new StubSource(10, 1) {
                @Override
                public Map<String, Model> describe(List<String> batch, String body) {
                    return Map.of();
                }
            };
            respondWith("nothing usable");

            val described = retriever.describe(List.of(THING + "a"), source);

            assertThat("nothing to publish", described.model().isEmpty(), is(true));
            assertThat("but nothing a later run can improve either, so it must not block",
                described.isComplete(), is(true));
            assertThat("and it is remembered",
                cache.get(THING + "a", Duration.ofDays(7)).isPresent(), is(true));
        }
    }

    @Nested
    @DisplayName("A batched response")
    class Batching {

        @Test
        @DisplayName("is split per entity, so one entity's statements are never published as another's")
        void splitsPerEntity() {
            val source = new StubSource(10, 3);
            respondWith("shared response");

            val described = retriever.describe(List.of(THING + "a", THING + "b"), source);

            assertThat(labelOf(described.model(), THING + "a"), is("shared response"));
            assertThat(labelOf(described.model(), THING + "b"), is("shared response"));
        }

        @Test
        @DisplayName("that fails falls back for every entity in it, not just the first")
        void wholeBatchFallsBack() {
            val source = new StubSource(10, 3);
            respondWith(HttpStatus.INTERNAL_SERVER_ERROR);

            val described = retriever.describe(
                List.of(THING + "a", THING + "b", THING + "c"), source);

            assertThat("500 entities can ride on one query",
                described.transientFailures(), is(3));
        }
    }

    @Test
    @DisplayName("an IRI that is not usable is dropped before it reaches a query or a cache key")
    void unusableIrisAreDropped() {
        // A concept URI is interpolated into a SPARQL VALUES clause, so a brace
        // makes the whole query a syntax error -- which returns nothing, which
        // the publish-whole-or-not-at-all guard turns into a frozen graph. One
        // bad keyword in one record would stop a whole vocabulary. This was
        // enforced by SkosConceptRetriever alone; it now covers every source.
        val source = new StubSource(10, 5);
        respondWith("fine");

        val described = retriever.describe(List.of(THING + "a", THING + "{bad}"), source);

        assertThat(source.asked, contains(List.of(THING + "a")));
        assertThat(described.isComplete(), is(true));
    }
}
