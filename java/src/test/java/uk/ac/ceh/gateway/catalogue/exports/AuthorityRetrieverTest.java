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
import uk.ac.ceh.gateway.catalogue.exports.AuthorityRetriever.Descriptions;

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
import static org.hamcrest.Matchers.nullValue;
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
    /** A field, so a test can start a fresh expectation set for a second run. */
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        dataset = DatasetFactory.create();
        cache = new DescriptionCache(dataset, Clock.fixed(NOW, ZoneOffset.UTC));
        restTemplate = new RestTemplate();
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
        @DisplayName("an authority that answers and describes nothing replaces a stale copy with nothing")
        void okButEmptyDoesNotFallBack() {
            // The one behaviour change the vocabularies feel. SKOS could not
            // tell "answered and holds nothing about this concept" from "the
            // fetch failed": a failed fetch was simply absent from its response
            // map, an OK-but-unmatched response yielded an empty model, and both
            // fell back to the any-age cached copy. Only a failure falls back
            // now, on the grounds that the package republishes what an authority
            // currently says -- so continuing to serve our old copy asserts
            // something it no longer does.
            //
            // The cost is accepted: a concept temporarily missing from a
            // vocabulary release loses its description until it returns. The
            // graph-level isEmpty() guard still prevents a whole graph emptying.
            val source = new StubSource(10, 1) {
                @Override
                public Map<String, Model> describe(List<String> batch, String body) {
                    return Map.of();
                }
            };
            val stale = ModelFactory.createDefaultModel();
            stale.add(stale.getResource(THING + "a"), RDFS.label, "last year's description");
            new DescriptionCache(dataset, Clock.fixed(Instant.parse("2024-09-07T10:00:00Z"), ZoneOffset.UTC))
                .put(THING + "a", stale);
            respondWith("a response describing nothing we asked about");

            val described = retriever.describe(List.of(THING + "a"), source);

            assertThat("the stale copy is not republished", described.model().isEmpty(), is(true));
            assertThat("and this does not hold the graph back, since no later run can help",
                described.isComplete(), is(true));
        }

        @Test
        @DisplayName("but a failure still falls back, which is the distinction that changed")
        void failureStillFallsBack() {
            val source = new StubSource(10, 1);
            val stale = ModelFactory.createDefaultModel();
            stale.add(stale.getResource(THING + "a"), RDFS.label, "last year's description");
            new DescriptionCache(dataset, Clock.fixed(Instant.parse("2024-09-07T10:00:00Z"), ZoneOffset.UTC))
                .put(THING + "a", stale);
            respondWith(HttpStatus.INTERNAL_SERVER_ERROR);

            val described = retriever.describe(List.of(THING + "a"), source);

            assertThat("a bad minute at a vocabulary server must not shrink the graph",
                labelOf(described.model(), THING + "a"), is("last year's description"));
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

    @Test
    @DisplayName("the configured threshold matches the one the tests and the @Value default use")
    void configuredThresholdAgreesWithTheDefault() throws Exception {
        // Two places state this number: the @Value inline default, which is the
        // one a context without the properties file gets, and application.properties,
        // which is where an operator looks and changes it. They can drift silently --
        // every test here would still pass on the constant while production ran on
        // something else.
        val properties = new java.util.Properties();
        try (val in = getClass().getResourceAsStream("/application.properties")) {
            properties.load(in);
        }

        assertThat(properties.getProperty("authorities.excuseAfterFailures"),
            is(String.valueOf(AuthorityRetriever.DEFAULT_EXCUSE_AFTER_FAILURES)));
    }

    @Nested
    @DisplayName("When one entity fails run after run")
    class PersistentlyFailing {

        /**
         * The shape of the ORCID case: pub.orcid.org returns a deterministic 500
         * for a well-formed identifier whose record is otherwise fine. Classified
         * transient, so before this it withheld the whole graph for ever.
         */
        private Descriptions runWith(HttpStatus status, StubSource source, String... iris) {
            server = MockRestServiceServer.createServer(restTemplate);
            for (int i = 0; i < iris.length; i++) {
                respondWith(status);
            }
            return retriever.describe(List.of(iris), source);
        }

        @Test
        @DisplayName("it holds the graph back at first, because a later run may well do better")
        void holdsBackWhileItMightRecover() {
            val source = new StubSource(10, 1);

            val first = runWith(HttpStatus.INTERNAL_SERVER_ERROR, source, THING + "a");

            assertThat(first.transientFailures(), is(1));
            assertThat("a graph must not publish while an entity might still arrive",
                first.isComplete(), is(false));
        }

        @Test
        @DisplayName("once the failure is persistent it stops holding the graph back")
        void excusedOnceItIsClearlyNotComing() {
            val source = new StubSource(10, 1);

            Descriptions last = null;
            for (var run = 0; run < AuthorityRetriever.DEFAULT_EXCUSE_AFTER_FAILURES; run++) {
                last = runWith(HttpStatus.INTERNAL_SERVER_ERROR, source, THING + "a");
            }

            assertThat("the fifth consecutive failure is the one that gives up on it",
                last.transientFailures(), is(0));
            assertThat("2,124 other researchers must not be kept out by one broken record",
                last.isComplete(), is(true));
        }

        @Test
        @DisplayName("the other entities in the graph are described as normal throughout")
        void doesNotCostTheRestOfTheGraph() {
            val source = new StubSource(10, 1);
            for (var run = 0; run < AuthorityRetriever.DEFAULT_EXCUSE_AFTER_FAILURES - 1; run++) {
                runWith(HttpStatus.INTERNAL_SERVER_ERROR, source, THING + "a");
            }

            server = MockRestServiceServer.createServer(restTemplate);
            respondWith(HttpStatus.INTERNAL_SERVER_ERROR);
            respondWith("Ada");
            val descriptions = retriever.describe(List.of(THING + "a", THING + "b"), source);

            assertThat(descriptions.isComplete(), is(true));
            assertThat(labelOf(descriptions.model(), THING + "b"), is("Ada"));
            assertThat("nothing may be invented for the entity that failed",
                labelOf(descriptions.model(), THING + "a"), is(nullValue()));
        }

        @Test
        @DisplayName("it is still asked for once excused, so it can come back on its own")
        void keepsAsking() {
            val source = new StubSource(10, 1);
            for (var run = 0; run < AuthorityRetriever.DEFAULT_EXCUSE_AFTER_FAILURES; run++) {
                runWith(HttpStatus.INTERNAL_SERVER_ERROR, source, THING + "a");
            }
            val askedWhileFailing = source.asked.size();

            server = MockRestServiceServer.createServer(restTemplate);
            respondWith("Grace");
            val recovered = retriever.describe(List.of(THING + "a"), source);

            assertThat("an excused entity that is never asked about can never recover",
                source.asked.size(), is(askedWhileFailing + 1));
            assertThat(labelOf(recovered.model(), THING + "a"), is("Grace"));
        }

        @Test
        @DisplayName("a success clears the count, so consecutive really means consecutive")
        void successResetsTheCount() {
            // Belt and braces rather than the only guard: an entity that has
            // succeeded even once is cached, and addHeld then rescues it from
            // every later failure regardless of any count -- see
            // aHeldCopyIsUsedBeforeAnyOfThisApplies. The reset matters for the
            // diagnostic being truthful, and so that a flaky entity cannot creep
            // towards a threshold across runs it actually succeeded on.
            val source = new StubSource(10, 1);
            for (var run = 0; run < AuthorityRetriever.DEFAULT_EXCUSE_AFTER_FAILURES - 1; run++) {
                runWith(HttpStatus.INTERNAL_SERVER_ERROR, source, THING + "a");
            }
            assertThat(cache.failures(THING + "a"),
                is(AuthorityRetriever.DEFAULT_EXCUSE_AFTER_FAILURES - 1));

            server = MockRestServiceServer.createServer(restTemplate);
            respondWith("Grace");
            retriever.describe(List.of(THING + "a"), source);

            assertThat("four failures then a success is a clean slate, not four of five",
                cache.failures(THING + "a"), is(0));
        }

        @Test
        @DisplayName("being rate limited is not counted against the entity")
        void rateLimitingIsNotTheEntitysFault() {
            // 429 says the authority is busy, not that this entity is broken. If it
            // counted, a fortnight of rate limiting would excuse the whole graph.
            val source = new StubSource(10, 1);

            for (var run = 0; run < AuthorityRetriever.DEFAULT_EXCUSE_AFTER_FAILURES + 2; run++) {
                runWith(HttpStatus.TOO_MANY_REQUESTS, source, THING + "a");
            }

            assertThat(cache.failures(THING + "a"), is(0));
            val last = runWith(HttpStatus.TOO_MANY_REQUESTS, source, THING + "a");
            assertThat("a rate limited run must still hold the graph back",
                last.isComplete(), is(false));
        }

        @Test
        @DisplayName("an entity never asked about is not counted against either")
        void budgetExhaustionIsNotTheEntitysFault() {
            // Deferred, not failed: the budget ran out before its turn. Counting it
            // would excuse the tail of any list longer than the per-run budget.
            val source = new StubSource(1, 1);

            for (var run = 0; run < AuthorityRetriever.DEFAULT_EXCUSE_AFTER_FAILURES + 2; run++) {
                server = MockRestServiceServer.createServer(restTemplate);
                respondWith("Ada");
                retriever.describe(List.of(THING + "a", THING + "b"), source);
            }

            assertThat("the entity beyond the budget was never asked about",
                cache.failures(THING + "b"), is(0));
        }

        @Test
        @DisplayName("a stale copy is used instead, so an entity with history is never excused")
        void aHeldCopyIsUsedBeforeAnyOfThisApplies() {
            val source = new StubSource(10, 1);
            server = MockRestServiceServer.createServer(restTemplate);
            respondWith("Ada");
            retriever.describe(List.of(THING + "a"), source);

            for (var run = 0; run < AuthorityRetriever.DEFAULT_EXCUSE_AFTER_FAILURES + 2; run++) {
                runWith(HttpStatus.INTERNAL_SERVER_ERROR, source, THING + "a");
            }

            val descriptions = runWith(HttpStatus.INTERNAL_SERVER_ERROR, source, THING + "a");
            assertThat("a name from a fortnight ago beats dropping the person",
                labelOf(descriptions.model(), THING + "a"), is("Ada"));
            assertThat(descriptions.isComplete(), is(true));
        }
    }
}
