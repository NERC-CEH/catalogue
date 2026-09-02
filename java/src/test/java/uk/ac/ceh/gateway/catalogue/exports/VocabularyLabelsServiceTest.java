package uk.ac.ceh.gateway.catalogue.exports;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.ac.ceh.gateway.catalogue.templateHelpers.UriNormaliser;
import uk.ac.ceh.gateway.catalogue.vocabularies.Keyword;

import java.io.StringReader;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Publishing the vocabulary labels the application already holds (dri-one #350)")
class VocabularyLabelsServiceTest {

    private static final String SKOS_CONCEPT = "http://www.w3.org/2004/02/skos/core#Concept";
    private static final String PREF_LABEL = "http://www.w3.org/2004/02/skos/core#prefLabel";
    private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    private static final String VOID_ENTITIES = "http://rdfs.org/ns/void#entities";
    private static final String GENERATED_AT = "http://www.w3.org/ns/prov#generatedAtTime";

    private static final String GEMET_GRAPH = "http://www.eionet.europa.eu/gemet/";
    private static final String ENVTHES_GRAPH = "http://vocabs.lter-europe.net/EnvThes/";
    private static final String CAST_GRAPH = "http://onto.nerc.ac.uk/CAST/";

    @Mock private SolrClient solrClient;
    private VocabularyLabelsService service;

    @BeforeEach
    void setUp() {
        service = new VocabularyLabelsService(
            solrClient,
            new UriNormaliser(),
            Clock.fixed(Instant.parse("2026-09-02T12:00:00Z"), ZoneOffset.UTC)
        );
    }

    @SneakyThrows
    private void givenSolrHolds(List<Keyword> keywords) {
        val response = mock(QueryResponse.class);
        given(solrClient.query(anyString(), any(SolrQuery.class), eq(POST))).willReturn(response);
        given(response.getBeans(Keyword.class)).willReturn(keywords);
    }

    @SneakyThrows
    private void givenSolrFails(Exception failure) {
        given(solrClient.query(anyString(), any(SolrQuery.class), eq(POST))).willThrow(failure);
    }

    /** Parses one graph's Turtle, so a malformed literal fails the test rather than shipping. */
    private Model parse(String turtle) {
        val model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new StringReader(turtle), null, Lang.TURTLE);
        return model;
    }

    private String labelOf(Model model, String conceptUri) {
        return model.listObjectsOfProperty(createResource(conceptUri), createProperty(PREF_LABEL))
            .next().asLiteral().getString();
    }

    @Nested
    @DisplayName("One graph per authority")
    class GraphPerAuthority {

        @Test
        @DisplayName("each vocabulary is published to the graph of the authority that minted it")
        void graphsAreNamedForTheirAuthority() {
            givenSolrHolds(List.of(
                new Keyword("archaeology", "gemet", "http://www.eionet.europa.eu/gemet/concept/530"),
                new Keyword("A horizon", "envThes", "http://vocabs.lter-europe.net/EnvThes/41"),
                new Keyword("nitrogen", "cast", "http://onto.nerc.ac.uk/CAST/273"),
                new Keyword("Air quality", "research-activity", "https://digital.ceh.ac.uk/vocab/ra/7"),
                new Keyword("Catchment", "fdri", "https://digital.ceh.ac.uk/vocab/fdri/3")
            ));

            assertThat(service.graphs().keySet(), containsInAnyOrder(
                GEMET_GRAPH, ENVTHES_GRAPH, CAST_GRAPH,
                "https://digital.ceh.ac.uk/vocab/ra/",
                "https://digital.ceh.ac.uk/vocab/fdri/"
            ));
        }

        @Test
        @DisplayName("nothing is published into the catalogue's own graph")
        void neverTheCatalogueGraph() {
            givenSolrHolds(List.of(
                new Keyword("archaeology", "gemet", "http://www.eionet.europa.eu/gemet/concept/530")
            ));

            assertTrue(
                service.graphs().keySet().stream().noneMatch(g -> g.contains("catalogue.ceh.ac.uk")),
                "attribution is the point: a source graph must never be the catalogue's"
            );
        }

        @Test
        @DisplayName("a vocabulary nobody has mapped to an authority is skipped, not guessed at")
        void unmappedVocabularyIsSkipped() {
            givenSolrHolds(List.of(
                new Keyword("archaeology", "gemet", "http://www.eionet.europa.eu/gemet/concept/530"),
                new Keyword("Something", "brand-new-vocab", "http://example.invalid/thing/1")
            ));

            val graphs = service.graphs();
            assertThat(graphs.keySet(), contains(GEMET_GRAPH));
            assertTrue(
                graphs.values().stream().noneMatch(ttl -> ttl.contains("example.invalid")),
                "landing a new vocabulary in the wrong authority's graph is worse than omitting it"
            );
        }
    }

    @Nested
    @DisplayName("What each graph contains")
    class GraphContents {

        @Test
        @DisplayName("every concept is typed and carries the authority's own label")
        void conceptsAreTypedAndLabelled() {
            givenSolrHolds(List.of(
                new Keyword("archaeology", "gemet", "http://www.eionet.europa.eu/gemet/concept/530")
            ));

            val model = parse(service.graphs().get(GEMET_GRAPH));
            val concept = createResource("http://www.eionet.europa.eu/gemet/concept/530");

            assertTrue(model.contains(concept, createProperty(RDF_TYPE), createResource(SKOS_CONCEPT)));
            assertThat(labelOf(model, concept.getURI()), is("archaeology"));
        }

        @Test
        @DisplayName("the graph describes itself: what it is, when it was taken, how much of it there is")
        void provenanceHeader() {
            givenSolrHolds(List.of(
                new Keyword("A horizon", "envThes", "http://vocabs.lter-europe.net/EnvThes/41"),
                new Keyword("B horizon", "envThes", "http://vocabs.lter-europe.net/EnvThes/42")
            ));

            val model = parse(service.graphs().get(ENVTHES_GRAPH));
            val graph = createResource(ENVTHES_GRAPH);

            assertThat(
                model.listObjectsOfProperty(graph, createProperty(VOID_ENTITIES)).next()
                    .asLiteral().getInt(),
                is(2)
            );
            assertThat(
                model.listObjectsOfProperty(graph, createProperty(GENERATED_AT)).next()
                    .asLiteral().getString(),
                is("2026-09-02T12:00:00Z")
            );
        }

        @Test
        @DisplayName("no licence is claimed, because none has been established")
        void noLicenceIsAsserted() {
            givenSolrHolds(List.of(
                new Keyword("archaeology", "gemet", "http://www.eionet.europa.eu/gemet/concept/530")
            ));

            val model = parse(service.graphs().get(GEMET_GRAPH));
            assertTrue(
                model.listObjectsOfProperty(createProperty("http://purl.org/dc/terms/license"))
                    .toList().isEmpty(),
                "claiming the wrong licence would be worse than claiming none"
            );
        }

        @Test
        @DisplayName("a concept URI is normalised, so it matches the node the catalogue graph uses")
        void conceptUrisAreNormalised() {
            // The harvest and the record templates must agree on the scheme or the
            // graphs cannot be joined at all - dri-one #350's whole point.
            givenSolrHolds(List.of(
                new Keyword("A horizon", "envThes", "https://vocabs.lter-europe.net/EnvThes/41")
            ));

            val model = parse(service.graphs().get(ENVTHES_GRAPH));
            assertTrue(
                model.containsResource(createResource("http://vocabs.lter-europe.net/EnvThes/41")),
                "EnvThes mints http; an https label would never join the catalogue's node"
            );
        }

        @Test
        @DisplayName("a label containing a backslash or a quote still produces parseable Turtle")
        void awkwardLabelsAreEscaped() {
            givenSolrHolds(List.of(
                new Keyword("ICA\\R1\\180100", "cast", "http://onto.nerc.ac.uk/CAST/1"),
                new Keyword("the \"quoted\" term", "cast", "http://onto.nerc.ac.uk/CAST/2"),
                new Keyword("two\nlines", "cast", "http://onto.nerc.ac.uk/CAST/3")
            ));

            // The parse is the assertion: an unescaped backslash in one literal
            // took down every export for a week (dri-one #344).
            val model = parse(service.graphs().get(CAST_GRAPH));

            assertThat(labelOf(model, "http://onto.nerc.ac.uk/CAST/1"), is("ICA\\R1\\180100"));
            assertThat(labelOf(model, "http://onto.nerc.ac.uk/CAST/2"), is("the 'quoted' term"));
            assertThat(labelOf(model, "http://onto.nerc.ac.uk/CAST/3"), is("two lines"));
        }

        @Test
        @DisplayName("a concept with an unusable URI or no label is left out rather than half-emitted")
        void unusableEntriesAreDropped() {
            givenSolrHolds(List.of(
                new Keyword("fine", "cast", "http://onto.nerc.ac.uk/CAST/1"),
                new Keyword("bad uri", "cast", "hhttp://onto.nerc.ac.uk/CAST/2"),
                new Keyword("", "cast", "http://onto.nerc.ac.uk/CAST/3"),
                new Keyword(null, "cast", "http://onto.nerc.ac.uk/CAST/4")
            ));

            val model = parse(service.graphs().get(CAST_GRAPH));
            assertThat(
                model.listObjectsOfProperty(createProperty(PREF_LABEL)).toList(),
                contains((RDFNode) model.createLiteral("fine"))
            );
        }
    }

    @Nested
    @DisplayName("When the keyword index cannot be read")
    class SolrFailure {

        @Test
        @DisplayName("a Solr failure publishes nothing rather than emptying the graphs")
        void solrFailurePublishesNothing() {
            givenSolrFails(new RuntimeException("connection refused"));

            assertThat(service.graphs(), is(Map.of()));
        }

        @Test
        @DisplayName("an empty index publishes nothing, so a previous run's graph is left alone")
        void emptyIndexPublishesNothing() {
            givenSolrHolds(List.of());

            assertThat(service.graphs(), is(Map.of()));
        }
    }
}
