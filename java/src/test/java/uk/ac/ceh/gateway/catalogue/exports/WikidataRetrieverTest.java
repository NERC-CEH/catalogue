package uk.ac.ceh.gateway.catalogue.exports;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Asking Wikidata in batches (dri-one #350 phase 5).
 *
 * <p>The fixture is a real Query Service response, and was chosen for one
 * property that a hand-written one would not have had: {@code Q1358}, Arachnida,
 * carries aliases and a taxon name but <em>no English label</em>. That is the
 * case the taxon name exists to rescue, and it is real rather than contrived.
 */
@DisplayName("Asking Wikidata about the concepts records use as subjects (dri-one #350)")
class WikidataRetrieverTest {

    private static final String ENDPOINT = "http://wikidata.invalid/sparql";
    private static final String WD = "http://www.wikidata.org/entity/";
    private static final String SPECKLED_WOOD = WD + "Q663181";
    private static final String ARACHNIDA = WD + "Q1358";
    private static final String MURRE = WD + "Q21062";
    private static final String TAXON = WD + "Q16521";
    private static final String P225 = "http://www.wikidata.org/prop/direct/P225";
    private static final String P31 = "http://www.wikidata.org/prop/direct/P31";
    private static final String USER_AGENT = "ukceh-catalogue-test/1.0 (test)";

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

    private WikidataRetriever retriever() {
        return retriever(500, 8);
    }

    private WikidataRetriever retriever(int batchSize, int queriesPerRun) {
        return new WikidataRetriever(
            restTemplate, cache, ENDPOINT, USER_AGENT, batchSize, queriesPerRun);
    }

    @SneakyThrows
    private static String fixture() {
        try (val in = WikidataRetrieverTest.class.getResourceAsStream("/exports/wikidata-batch.ttl")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static List<String> entities(int count) {
        return IntStream.range(0, count).mapToObj(i -> WD + "Q" + (1000 + i)).toList();
    }

    @Nested
    @DisplayName("Which URIs are worth asking about")
    class Selection {

        @Test
        @DisplayName("an entity id is claimed")
        void entityIsClaimed() {
            assertTrue(WikidataRetriever.describes(SPECKLED_WOOD));
        }

        @Test
        @DisplayName("the two malformed URIs in production are not")
        void malformedUrisAreRejected() {
            // Both are really in the graph. Neither will ever resolve, and
            // asking would remember an empty answer for a typo.
            assertFalse(WikidataRetriever.describes(WD + "(Q1054552"),
                "a stray opening parenthesis");
            assertFalse(WikidataRetriever.describes("http://www.wikidata.org/entity"),
                "a bare namespace with no id at all");
        }

        @Test
        @DisplayName("nor is anything else under the namespace")
        void otherFormsAreRejected() {
            assertFalse(WikidataRetriever.describes(WD + "Qfoo"));
            assertFalse(WikidataRetriever.describes(WD + "P31"), "a property is not an entity");
            assertFalse(WikidataRetriever.describes(WD + "Q0"), "an id does not start with zero");
            assertFalse(WikidataRetriever.describes("https://www.wikidata.org/entity/Q663181"),
                "the https form is not what Wikidata mints; UriNormaliser converges it");
        }
    }

    @Nested
    @DisplayName("Batching")
    class Batching {

        @Test
        @DisplayName("the whole set is one query, not one per entity")
        void oneQueryForTheBatch() {
            server.expect(once(), requestTo(ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.USER_AGENT, USER_AGENT))
                .andRespond(withSuccess(fixture(), MediaType.valueOf("text/turtle")));

            val described = retriever().describe(List.of(SPECKLED_WOOD, ARACHNIDA, MURRE));

            // once() means a second request fails verification. Dereferencing
            // these individually would move 324KB each.
            server.verify();
            assertTrue(described.model().contains(
                createResource(SPECKLED_WOOD), RDFS.label, "Speckled Wood", "en"));
        }

        @Test
        @DisplayName("every entity asked for appears in the query")
        void entitiesAreNamedInTheQuery() {
            server.expect(requestTo(ENDPOINT))
                .andExpect(content().string(containsString("<" + SPECKLED_WOOD + ">")))
                .andExpect(content().string(containsString("<" + ARACHNIDA + ">")))
                .andRespond(withSuccess(fixture(), MediaType.valueOf("text/turtle")));

            retriever().describe(List.of(SPECKLED_WOOD, ARACHNIDA));

            server.verify();
        }

        @Test
        @DisplayName("a set larger than the batch size is split across queries")
        void largeSetIsSplit() {
            // 250 entities at a batch size of 100 is three queries: a fourth
            // would find no expectation and throw.
            IntStream.range(0, 3).forEach(i ->
                server.expect(requestTo(ENDPOINT))
                    .andRespond(withSuccess(fixture(), MediaType.valueOf("text/turtle"))));

            retriever(100, 8).describe(entities(250));

            server.verify();
        }

        @Test
        @DisplayName("the query budget is counted in queries, not entities")
        void budgetLimitsQueries() {
            server.expect(once(), requestTo(ENDPOINT))
                .andRespond(withSuccess(fixture(), MediaType.valueOf("text/turtle")));

            val described = retriever(100, 1).describe(entities(250));

            server.verify();
            assertThat("the two unqueried batches are left for the next run",
                described.deferred(), is(150));
            assertThat(described.isComplete(), is(false));
        }
    }

    @Nested
    @DisplayName("What each entity ends up with")
    class Descriptions {

        @Test
        @DisplayName("an entity gets its own statements and nothing from its neighbours")
        void statementsAreSplitPerEntity() {
            server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess(fixture(), MediaType.valueOf("text/turtle")));
            retriever().describe(List.of(SPECKLED_WOOD, ARACHNIDA, MURRE));

            val held = cache.get(SPECKLED_WOOD, Duration.ofDays(1)).orElseThrow();

            assertTrue(held.contains(createResource(SPECKLED_WOOD), RDFS.label, "Speckled Wood", "en"));
            assertFalse(
                held.containsResource(createResource(MURRE)),
                "one response describes the whole batch, so the split has to be by subject"
            );
        }

        @Test
        @DisplayName("the type is named, not just pointed at")
        void typeLabelIsIncluded() {
            server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess(fixture(), MediaType.valueOf("text/turtle")));

            val model = retriever().describe(List.of(SPECKLED_WOOD)).model();

            assertTrue(model.contains(createResource(SPECKLED_WOOD),
                createProperty(P31), createResource(TAXON)));
            assertTrue(
                model.contains(createResource(TAXON), RDFS.label, "taxon", "en"),
                "otherwise a consumer reading 'instance of Q16521' learns nothing"
            );
        }

        @Test
        @DisplayName("an entity with no English label still gets a usable description")
        void taxonNameRescuesTheUnlabelled() {
            // Q1358 is Arachnida, and it really has no English label. 10 of a
            // 500-entity sample are like this, and the scientific name is the
            // only name half of them have.
            server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess(fixture(), MediaType.valueOf("text/turtle")));

            val model = retriever().describe(List.of(ARACHNIDA)).model();
            val arachnida = createResource(ARACHNIDA);

            assertFalse(model.contains(arachnida, RDFS.label), "no English label exists");
            assertTrue(model.contains(arachnida, createProperty(P225), "Arachnida"),
                "but the taxon name does");
            assertTrue(model.contains(arachnida, SKOS.altLabel, "arachnid", "en"),
                "and the common-name aliases a keyword search would need");
        }

        @Test
        @DisplayName("labels keep the language Wikidata recorded")
        void labelsAreLanguageTagged() {
            server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess(fixture(), MediaType.valueOf("text/turtle")));

            val model = retriever().describe(List.of(MURRE)).model();

            assertThat(
                model.listObjectsOfProperty(createResource(MURRE), RDFS.label)
                    .next().asLiteral().getLanguage(),
                is("en")
            );
        }
    }

    @Nested
    @DisplayName("Remembering the answers")
    class Caching {

        @Test
        @DisplayName("a second run asks nothing")
        void secondRunAsksNothing() {
            server.expect(once(), requestTo(ENDPOINT))
                .andRespond(withSuccess(fixture(), MediaType.valueOf("text/turtle")));

            val retriever = retriever();
            retriever.describe(List.of(SPECKLED_WOOD, ARACHNIDA, MURRE));
            val second = retriever.describe(List.of(SPECKLED_WOOD, ARACHNIDA, MURRE));

            server.verify();
            assertTrue(second.model().contains(
                createResource(SPECKLED_WOOD), RDFS.label, "Speckled Wood", "en"));
            assertThat(second.isComplete(), is(true));
        }

        @Test
        @DisplayName("an entity Wikidata does not hold is remembered as such")
        void deletedEntityIsRemembered() {
            // A merged or deleted entity is simply absent from the response.
            // Asking again every run for 2,064 entities' worth of those would
            // be a daily waste on both sides.
            val gone = WD + "Q999999999";
            server.expect(once(), requestTo(ENDPOINT))
                .andRespond(withSuccess(fixture(), MediaType.valueOf("text/turtle")));

            val retriever = retriever();
            retriever.describe(List.of(gone));
            retriever.describe(List.of(gone));

            server.verify();
        }

        @Test
        @DisplayName("an absent entity does not hold the graph back")
        void absentEntityDoesNotBlockPublishing() {
            server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess(fixture(), MediaType.valueOf("text/turtle")));

            val described = retriever().describe(List.of(SPECKLED_WOOD, WD + "Q999999999"));

            assertThat(
                "a deleted entity is not something a later run can fix",
                described.isComplete(), is(true)
            );
        }
    }

    @Nested
    @DisplayName("When the service pushes back")
    class PushBack {

        @Test
        @DisplayName("a failed batch costs 500 entities, so it holds the graph back")
        void failedBatchHoldsTheGraphBack() {
            server.expect(requestTo(ENDPOINT)).andRespond(withServerError());

            val described = retriever().describe(List.of(SPECKLED_WOOD, ARACHNIDA));

            assertThat(described.transientFailures(), is(2));
            assertThat(described.isComplete(), is(false));
        }

        @Test
        @DisplayName("a 429 is reported rather than swallowed")
        void rateLimitIsHandled() {
            server.expect(requestTo(ENDPOINT))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

            val described = retriever().describe(List.of(SPECKLED_WOOD));

            assertThat(described.transientFailures(), is(1));
        }

        @Test
        @DisplayName("a copy of any age beats losing the entities from the graph")
        void staleBeatsNothing() {
            server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess(fixture(), MediaType.valueOf("text/turtle")));
            retriever().describe(List.of(SPECKLED_WOOD));

            val later = MockRestServiceServer.createServer(restTemplate);
            later.expect(requestTo(ENDPOINT)).andRespond(withServerError());
            val aged = new DescriptionCache(dataset,
                Clock.fixed(java.time.Instant.now().plus(Duration.ofDays(60)),
                    java.time.ZoneOffset.UTC));

            val described = new WikidataRetriever(
                restTemplate, aged, ENDPOINT, USER_AGENT, 500, 8)
                .describe(List.of(SPECKLED_WOOD));

            assertTrue(described.model().contains(
                createResource(SPECKLED_WOOD), RDFS.label, "Speckled Wood", "en"));
            assertThat("nothing is missing, so the graph may still publish",
                described.isComplete(), is(true));
        }

        @Test
        @DisplayName("a response that is not RDF is a transient failure, not a silent one")
        void unparseableResponseIsTransient() {
            server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess("<html>service unavailable</html>", MediaType.TEXT_HTML));

            val described = retriever().describe(List.of(SPECKLED_WOOD));

            assertThat(described.transientFailures(), is(1));
            assertThat(described.model().size(), is(0L));
        }
    }

    @Nested
    @DisplayName("Being a considerate client")
    class Etiquette {

        @Test
        @DisplayName("a user agent identifying us is sent, because WDQS blocks clients without one")
        void userAgentIsSent() {
            server.expect(requestTo(ENDPOINT))
                .andExpect(header(HttpHeaders.USER_AGENT, USER_AGENT))
                .andRespond(withSuccess(fixture(), MediaType.valueOf("text/turtle")));

            retriever().describe(List.of(SPECKLED_WOOD));

            server.verify();
        }

        @Test
        @DisplayName("the configured budget can fill the referenced set inside its refresh age")
        void budgetConverges() throws Exception {
            // The convergence rule from phase 3, applied to queries rather than
            // entities: a first fill must finish comfortably inside MAX_AGE or
            // the entities fetched first go stale before the last are reached.
            val properties = new java.util.Properties();
            try (var in = getClass().getResourceAsStream("/application.properties")) {
                properties.load(in);
            }
            val batchSize = Integer.parseInt(properties.getProperty("wikidata.batchSize"));
            val queriesPerRun = Integer.parseInt(properties.getProperty("wikidata.queriesPerRun"));
            val entities = 2064;
            val runsToFill = (entities + (batchSize * queriesPerRun) - 1) / (batchSize * queriesPerRun);

            assertThat("the whole set should fill in a single run", runsToFill, is(1));
            assertTrue(runsToFill < WikidataRetriever.MAX_AGE.toDays());
        }

        @Test
        @DisplayName("nothing referenced means no query at all")
        void nothingReferencedMeansNoQuery() {
            val described = retriever().describe(List.of());

            server.verify();
            assertThat(described.model().size(), is(0L));
            assertThat(described.isComplete(), is(true));
        }
    }
}
