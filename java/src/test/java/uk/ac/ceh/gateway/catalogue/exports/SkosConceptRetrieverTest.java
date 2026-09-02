package uk.ac.ceh.gateway.catalogue.exports;

import lombok.val;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    private MockRestServiceServer server;
    private SkosConceptRetriever retriever;

    @BeforeEach
    void setUp() {
        val restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        retriever = new SkosConceptRetriever(restTemplate, SPARQL);
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
}
