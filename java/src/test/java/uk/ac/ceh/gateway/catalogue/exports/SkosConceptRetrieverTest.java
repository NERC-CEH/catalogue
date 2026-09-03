package uk.ac.ceh.gateway.catalogue.exports;

import lombok.val;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb2.TDB2Factory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import java.util.List;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("Retrieving what an authority says about its own concepts (dri-one #350)")
class SkosConceptRetrieverTest {

    private static final String NVS = "http://vocab.nerc.ac.uk/collection/P07/current/CFSN0381/";
    private static final String OTHER = "http://vocab.nerc.ac.uk/collection/P07/current/OTHER/";
    private static final String SPARQL = "http://vocabs.invalid/query";
    private static final String CAST = "http://onto.nerc.ac.uk/CAST/273";

    private MockRestServiceServer server;
    private SkosConceptRetriever retriever;
    private RestTemplate restTemplate;
    private Dataset dataset;
    private DescriptionCache cache;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        dataset = TDB2Factory.createDataset();
        cache = new DescriptionCache(dataset, Clock.systemUTC());
        retriever = new SkosConceptRetriever(restTemplate, SPARQL, cache);
    }

    @AfterEach
    void tearDown() {
        dataset.close();
    }

    /**
     * A retriever sharing this test's cache but with its own clock, standing in
     * for a later export run.
     */
    private SkosConceptRetriever laterRun(Duration after) {
        return new SkosConceptRetriever(restTemplate, SPARQL,
            new DescriptionCache(dataset, Clock.fixed(Instant.now().plus(after), ZoneOffset.UTC)));
    }

    /** A response shaped like NVS's: the concept, plus a lot we did not ask for. */
    private static String nvsResponse() {
        return """
            @prefix skos: <http://www.w3.org/2004/02/skos/core#> .
            @prefix dc:   <http://purl.org/dc/terms/> .
            @prefix owl:  <http://www.w3.org/2002/07/owl#> .
            @prefix pav:  <http://purl.org/pav/> .

            <%s> a skos:Concept ;
                skos:prefLabel "sea water temperature" ;
                skos:altLabel "water temp" ;
                skos:definition "The temperature of sea water." ;
                skos:broader <http://vocab.nerc.ac.uk/collection/P07/current/BROADER/> ;
                skos:notation "SDN:P07::CFSN0381" ;
                dc:date "2024-05-20" ;
                owl:sameAs <http://example.invalid/elsewhere> ;
                pav:hasCurrentVersion [ a skos:Concept ; skos:prefLabel "a nested thing" ] .

            <http://vocab.nerc.ac.uk/collection/P07/current/UNRELATED/>
                a skos:Concept ; skos:prefLabel "something else entirely" .
            """.formatted(NVS);
    }

    /** A second concept, so a batch can hold one held copy and one to fetch. */
    private static String otherResponse() {
        return """
            @prefix skos: <http://www.w3.org/2004/02/skos/core#> .

            <%s> a skos:Concept ;
                skos:prefLabel "salinity" ;
                skos:definition "The salt content of sea water." .
            """.formatted(OTHER);
    }

    /** A CAST concept, for the batch-query path. */
    private static String castResponse() {
        return """
            @prefix skos: <http://www.w3.org/2004/02/skos/core#> .

            <%s> a skos:Concept ;
                skos:prefLabel "nitrogen" ;
                skos:altLabel "N" .
            """.formatted(CAST);
    }

    /** OTHER's response, pointing at another concept as its broader term. */
    private static String otherResponseNaming(String broader) {
        return """
            @prefix skos: <http://www.w3.org/2004/02/skos/core#> .

            <%s> a skos:Concept ;
                skos:prefLabel "salinity" ;
                skos:broader <%s> .
            """.formatted(OTHER, broader);
    }

    @Nested
    @DisplayName("Dereferencing the concept URI")
    class ContentNegotiation {

        @Test
        @DisplayName("the concept's SKOS description is kept")
        void keepsTheSkosDescription() {
            server.expect(requestTo(NVS))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Accept", "text/turtle"))
                .andRespond(withSuccess(nvsResponse(), MediaType.valueOf("text/turtle")));

            val model = retriever.describe(List.of(NVS), SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION);
            val concept = createResource(NVS);

            server.verify();
            assertTrue(model.contains(concept, RDF.type, SKOS.Concept));
            assertTrue(model.contains(concept, SKOS.prefLabel, "sea water temperature"));
            assertTrue(model.contains(concept, SKOS.altLabel, "water temp"));
            assertTrue(model.contains(concept, SKOS.definition, "The temperature of sea water."));
            assertTrue(model.contains(concept, SKOS.notation, "SDN:P07::CFSN0381"));
            assertTrue(model.contains(concept, SKOS.broader,
                createResource("http://vocab.nerc.ac.uk/collection/P07/current/BROADER/")));
        }

        @Test
        @DisplayName("everything else the authority sent is discarded")
        void discardsEverythingElse() {
            server.expect(requestTo(NVS))
                .andRespond(withSuccess(nvsResponse(), MediaType.valueOf("text/turtle")));

            val model = retriever.describe(List.of(NVS), SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION);

            assertFalse(
                model.contains(createResource(NVS),
                    createProperty("http://purl.org/dc/terms/date"), "2024-05-20"),
                "registry bookkeeping is the authority's business, not ours to mirror"
            );
            assertFalse(
                model.containsResource(createResource("http://example.invalid/elsewhere")),
                "a mapping to a third-party vocabulary is not a description of this concept"
            );
            assertFalse(
                model.containsResource(
                    createResource("http://vocab.nerc.ac.uk/collection/P07/current/UNRELATED/")),
                "only the concept asked for should appear"
            );
            assertTrue(
                model.listObjects().toList().stream().noneMatch(node -> node.isAnon()),
                "a blank node would drag the authority's internal structure in behind it"
            );
        }

        @Test
        @DisplayName("one unreachable concept does not cost the others")
        void oneFailureDoesNotCostTheRest() {
            server.expect(requestTo(NVS))
                .andRespond(withSuccess(nvsResponse(), MediaType.valueOf("text/turtle")));
            server.expect(requestTo(OTHER)).andRespond(withServerError());

            val model = retriever.describe(
                List.of(NVS, OTHER), SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION);

            server.verify();
            assertTrue(model.contains(createResource(NVS), SKOS.prefLabel, "sea water temperature"));
            assertFalse(model.containsResource(createResource(OTHER)));
        }

        @Test
        @DisplayName("a response that is not RDF is skipped rather than thrown")
        void unparseableResponseIsSkipped() {
            server.expect(requestTo(NVS))
                .andRespond(withSuccess("<html>service unavailable</html>", MediaType.TEXT_HTML));

            val model = retriever.describe(List.of(NVS), SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION);

            assertThat("a vocabulary server serving an error page must not fail the export",
                model.size(), is(0L));
        }
    }

    @Nested
    @DisplayName("Querying the UKCEH vocabulary server")
    class UkcehSparql {

        @Test
        @DisplayName("the whole batch is one request, not one per concept")
        void oneRequestForTheBatch() {
            val cast1 = "http://onto.nerc.ac.uk/CAST/273";
            val cast2 = "http://onto.nerc.ac.uk/CAST/274";
            val response = """
                @prefix skos: <http://www.w3.org/2004/02/skos/core#> .
                <%s> a skos:Concept ; skos:prefLabel "nitrogen" ; skos:altLabel "N" .
                <%s> a skos:Concept ; skos:prefLabel "phosphorus" .
                """.formatted(cast1, cast2);

            // One expectation: a second request would fail verification.
            server.expect(requestTo(org.hamcrest.Matchers.startsWith(SPARQL + "?query=")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(response, MediaType.valueOf("text/turtle")));

            val model = retriever.describe(
                List.of(cast1, cast2), SkosConceptRetriever.Retrieval.UKCEH_SPARQL);

            server.verify();
            assertTrue(model.contains(createResource(cast1), SKOS.prefLabel, "nitrogen"));
            assertTrue(model.contains(createResource(cast1), SKOS.altLabel, "N"));
            assertTrue(model.contains(createResource(cast2), SKOS.prefLabel, "phosphorus"));
        }

        @Test
        @DisplayName("an empty batch asks nothing at all")
        void emptyBatchAsksNothing() {
            val model = retriever.describe(List.of(), SkosConceptRetriever.Retrieval.UKCEH_SPARQL);

            server.verify();
            assertThat(model.size(), is(0L));
        }

        @Test
        @DisplayName("an endpoint failure yields nothing rather than propagating")
        void endpointFailureYieldsNothing() {
            server.expect(requestTo(org.hamcrest.Matchers.startsWith(SPARQL + "?query=")))
                .andRespond(withServerError());

            val model = retriever.describe(
                List.of("http://onto.nerc.ac.uk/CAST/273"), SkosConceptRetriever.Retrieval.UKCEH_SPARQL);

            assertThat(model.size(), is(0L));
        }
    }

    @Nested
    @DisplayName("A concept URI that is not a usable IRI")
    class UnusableConceptUris {

        @Test
        @DisplayName("is dropped before the query is built, so it cannot cost the whole batch")
        void unusableUriDoesNotBreakTheBatch() {
            // The URI is interpolated into the VALUES clause, so a brace makes
            // the whole CONSTRUCT a syntax error -- the endpoint returns 400,
            // describe returns nothing, and the caller's
            // publish-whole-or-not-at-all guard then freezes the graph
            // indefinitely. One bad keyword in one record would stop CAST for
            // good.
            val bad = "http://onto.nerc.ac.uk/CAST/{broken}";
            server.expect(requestTo(org.hamcrest.Matchers.startsWith(SPARQL)))
                // The assertion that matters: the brace must never reach the
                // query at all. Checking only that the good concept came back
                // would pass even if the bad URI were interpolated, because this
                // mock answers whatever it is sent -- a real endpoint would 400.
                // Not a check for an encoded brace: the query text itself is full
                // of them (CONSTRUCT { ... }, VALUES ?concept { ... }). What must
                // be absent is this concept.
                .andExpect(request -> assertFalse(
                    request.getURI().toString().contains("broken"),
                    "an unusable concept URI must not be built into the SPARQL query"))
                .andRespond(withSuccess(castResponse(), MediaType.valueOf("text/turtle")));

            val model = retriever.describe(
                List.of(CAST, bad), SkosConceptRetriever.Retrieval.UKCEH_SPARQL);

            server.verify();
            assertTrue(
                model.contains(createResource(CAST), SKOS.prefLabel, "nitrogen"),
                "the good concept is still described"
            );
            assertFalse(model.containsResource(createResource(bad)));
        }

        @Test
        @DisplayName("is not asked for over HTTP either, since it cannot be a URL")
        void unusableUriIsNotDereferenced() {
            val bad = "http://vocab.nerc.ac.uk/collection/P07/current/pi|pe/";
            server.expect(once(), requestTo(NVS))
                .andRespond(withSuccess(nvsResponse(), MediaType.valueOf("text/turtle")));

            val model = retriever.describe(
                List.of(NVS, bad), SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION);

            // Only NVS is expected, so an attempt on the bad URI would throw.
            server.verify();
            assertTrue(model.containsResource(createResource(NVS)));
        }
    }

    @Nested
    @DisplayName("Keeping what was said, so a bad minute does not cost content")
    class Caching {

        @Test
        @DisplayName("a second run does not ask the authority again")
        void secondRunUsesTheCache() {
            server.expect(once(), requestTo(NVS))
                .andRespond(withSuccess(nvsResponse(), MediaType.valueOf("text/turtle")));

            retriever.describe(List.of(NVS), SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION);
            val second = retriever.describe(
                List.of(NVS), SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION);

            // once() means a second request fails verification.
            server.verify();
            assertTrue(second.contains(
                createResource(NVS), SKOS.prefLabel, "sea water temperature"));
        }

        @Test
        @DisplayName("a concept is asked for again once its copy has aged")
        void staleCopyIsRefetched() {
            server.expect(requestTo(NVS))
                .andRespond(withSuccess(nvsResponse(), MediaType.valueOf("text/turtle")));
            retriever.describe(List.of(NVS), SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION);

            val later = MockRestServiceServer.createServer(restTemplate);
            later.expect(requestTo(NVS))
                .andRespond(withSuccess(nvsResponse(), MediaType.valueOf("text/turtle")));

            laterRun(Duration.ofDays(8))
                .describe(List.of(NVS), SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION);

            // A vocabulary can be revised on any release, so a week-old copy is
            // refreshed rather than kept indefinitely.
            later.verify();
        }

        @Test
        @DisplayName("a concept the authority cannot serve today keeps the copy from last time")
        void unreachableConceptFallsBackToTheStoredCopy() {
            server.expect(requestTo(NVS))
                .andRespond(withSuccess(nvsResponse(), MediaType.valueOf("text/turtle")));
            retriever.describe(List.of(NVS), SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION);

            // A later run, past the refresh age, where the authority is down.
            val later = MockRestServiceServer.createServer(restTemplate);
            later.expect(requestTo(NVS)).andRespond(withServerError());

            val model = laterRun(Duration.ofDays(8))
                .describe(List.of(NVS), SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION);

            assertTrue(
                model.contains(createResource(NVS), SKOS.prefLabel, "sea water temperature"),
                "this is the case that used to shrink a published graph: without a "
                    + "stored copy the concept simply vanished from the graph"
            );
        }

        @Test
        @DisplayName("a concept never successfully retrieved is simply absent")
        void neverRetrievedIsAbsent() {
            server.expect(requestTo(NVS)).andRespond(withServerError());

            val model = retriever.describe(
                List.of(NVS), SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION);

            assertTrue(model.isEmpty(), "there is nothing to fall back on, and none is invented");
        }

        @Test
        @DisplayName("a failed retrieval is not stored, so the next run tries again")
        void failuresAreNotCached() {
            server.expect(requestTo(NVS)).andRespond(withServerError());
            retriever.describe(List.of(NVS), SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION);

            val later = MockRestServiceServer.createServer(restTemplate);
            later.expect(requestTo(NVS))
                .andRespond(withSuccess(nvsResponse(), MediaType.valueOf("text/turtle")));

            val model = retriever.describe(
                List.of(NVS), SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION);

            // Caching "the authority said nothing" here would turn one bad minute
            // into a week of silence about that concept.
            later.verify();
            assertTrue(model.contains(createResource(NVS), SKOS.prefLabel, "sea water temperature"));
        }

        @Test
        @DisplayName("a neighbour mentioning a concept does not count as describing it")
        void beingMentionedIsNotBeingDescribed() {
            // NVS and AGROVOC hierarchies mean both ends of a skos:broader are
            // routinely in the referenced set, so this is the common case rather
            // than a contrived one. NVS is cached well, then ages out; its
            // refetch fails while OTHER succeeds with a response naming it.
            server.expect(requestTo(NVS))
                .andRespond(withSuccess(nvsResponse(), MediaType.valueOf("text/turtle")));
            retriever.describe(List.of(NVS), SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION);

            val later = MockRestServiceServer.createServer(restTemplate);
            later.expect(requestTo(NVS)).andRespond(withServerError());
            later.expect(requestTo(OTHER))
                .andRespond(withSuccess(otherResponseNaming(NVS), MediaType.valueOf("text/turtle")));

            val model = laterRun(Duration.ofDays(8)).describe(
                List.of(NVS, OTHER), SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION);

            assertTrue(
                model.contains(createResource(NVS), SKOS.prefLabel, "sea water temperature"),
                "NVS's own failed fetch must fall back to the stored copy; being named "
                    + "as OTHER's broader concept must not make it look freshly described"
            );
            assertFalse(
                model.contains(createResource(NVS), SKOS.narrower, createResource(OTHER)),
                "and nothing from OTHER's response may be published as NVS's own assertion"
            );
        }

        @Test
        @DisplayName("a good stored description is not overwritten by a failed refetch")
        void failedRefetchDoesNotReplaceTheStoredCopy() {
            server.expect(requestTo(NVS))
                .andRespond(withSuccess(nvsResponse(), MediaType.valueOf("text/turtle")));
            retriever.describe(List.of(NVS), SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION);

            val later = MockRestServiceServer.createServer(restTemplate);
            later.expect(requestTo(NVS)).andRespond(withServerError());
            later.expect(requestTo(OTHER))
                .andRespond(withSuccess(otherResponseNaming(NVS), MediaType.valueOf("text/turtle")));

            laterRun(Duration.ofDays(8)).describe(
                List.of(NVS, OTHER), SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION);

            // Had the failed refetch been cached, the damage would outlive the
            // run: a bare type triple stamped fresh, hiding the good copy for a
            // week.
            val held = cache.get(NVS, Duration.ofDays(14)).orElseThrow();
            assertTrue(
                held.contains(createResource(NVS), SKOS.prefLabel, "sea water temperature"),
                "the stored description should still be the one the authority gave us"
            );
        }

        @Test
        @DisplayName("only the concepts not already held are asked for")
        void onlyMissingConceptsAreFetched() {
            server.expect(once(), requestTo(NVS))
                .andRespond(withSuccess(nvsResponse(), MediaType.valueOf("text/turtle")));
            retriever.describe(List.of(NVS), SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION);

            val later = MockRestServiceServer.createServer(restTemplate);
            later.expect(once(), requestTo(OTHER))
                .andRespond(withSuccess(otherResponse(), MediaType.valueOf("text/turtle")));

            val model = retriever.describe(
                List.of(NVS, OTHER), SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION);

            // Only OTHER is expected, so a repeat request for NVS would throw.
            later.verify();
            assertTrue(model.containsResource(createResource(NVS)), "the held one is still returned");
            assertTrue(model.containsResource(createResource(OTHER)));
        }
    }
}
