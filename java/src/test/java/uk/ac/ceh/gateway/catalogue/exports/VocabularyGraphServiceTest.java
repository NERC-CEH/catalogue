package uk.ac.ceh.gateway.catalogue.exports;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
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
import java.util.Set;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Assembling a named graph per vocabulary authority (dri-one #350)")
class VocabularyGraphServiceTest {

    private static final String GEMET = "http://www.eionet.europa.eu/gemet/";
    private static final String ENVTHES = "http://vocabs.lter-europe.net/EnvThes/";
    private static final String CAST = "http://onto.nerc.ac.uk/CAST/";
    private static final String NVS = "http://vocab.nerc.ac.uk/";
    private static final String AGROVOC = "http://aims.fao.org/aos/agrovoc/";

    private static final String CAST_NITROGEN = CAST + "273";
    private static final String NVS_CONCEPT = NVS + "collection/P07/current/CFSN0381/";

    @Mock private SolrClient solrClient;
    @Mock private SkosConceptRetriever retriever;
    private VocabularyGraphService service;

    @BeforeEach
    void setUp() {
        service = new VocabularyGraphService(
            solrClient,
            new UriNormaliser(),
            retriever,
            Clock.fixed(Instant.parse("2026-09-02T12:00:00Z"), ZoneOffset.UTC)
        );
    }

    @SneakyThrows
    private void givenHarvestedLabels(List<Keyword> keywords) {
        val response = mock(QueryResponse.class);
        given(solrClient.query(anyString(), any(SolrQuery.class), eq(POST))).willReturn(response);
        given(response.getBeans(Keyword.class)).willReturn(keywords);
    }

    private void givenRetrieverReturns(Model model) {
        given(retriever.describe(any(), any())).willReturn(model);
    }

    private static Model skosFor(String conceptUri, String prefLabel, String definition, String broader) {
        val model = ModelFactory.createDefaultModel();
        val concept = model.getResource(conceptUri);
        model.add(concept, RDF.type, SKOS.Concept);
        model.add(concept, SKOS.prefLabel, prefLabel);
        model.add(concept, SKOS.definition, definition);
        model.add(concept, SKOS.broader, model.getResource(broader));
        return model;
    }

    private Model parse(String turtle) {
        val model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new StringReader(turtle), null, Lang.TURTLE);
        return model;
    }

    @Nested
    @DisplayName("Harvested labels and retrieved descriptions reach the same graph")
    class BothSourcesMerge {

        @Test
        @DisplayName("CAST is fed by both, and neither source overwrites the other")
        void castMergesLabelsAndDescriptions() {
            // CAST is the one vocabulary with both a keyword harvest and a
            // retrievable description. Before these were assembled together, two
            // writers aimed at one graph and the second silently replaced the first.
            givenHarvestedLabels(List.of(
                new Keyword("nitrogen", "cast", CAST_NITROGEN),
                new Keyword("phosphorus", "cast", CAST + "274")
            ));
            givenRetrieverReturns(skosFor(CAST_NITROGEN, "nitrogen", "The element N.", CAST + "1"));

            val model = parse(service.graphs(Set.of(CAST_NITROGEN)).get(CAST));
            val nitrogen = createResource(CAST_NITROGEN);

            assertTrue(model.contains(nitrogen, SKOS.definition, "The element N."),
                "the retrieved definition should survive");
            assertTrue(model.contains(nitrogen, SKOS.broader, createResource(CAST + "1")),
                "so should the retrieved hierarchy");
            assertTrue(model.contains(createResource(CAST + "274"), SKOS.prefLabel, "phosphorus"),
                "and the harvested label for a concept nothing referenced");
        }

        @Test
        @DisplayName("a label-only vocabulary is never fetched")
        void labelOnlyVocabularyIsNotFetched() {
            givenHarvestedLabels(List.of(
                new Keyword("archaeology", "gemet", GEMET + "concept/530")
            ));

            val graphs = service.graphs(Set.of(GEMET + "concept/530"));

            assertThat(graphs.keySet(), contains(GEMET));
            verify(retriever, never()).describe(any(), eq(SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION));
        }
    }

    @Nested
    @DisplayName("Retrieval is limited to concepts something cites")
    class OnlyReferencedConcepts {

        @Test
        @DisplayName("a vocabulary with no harvest and no referenced concepts produces no graph")
        void nothingReferencedMeansNoGraph() {
            givenHarvestedLabels(List.of());

            assertThat(service.graphs(Set.of()), is(Map.of()));
            verify(retriever, never()).describe(any(), any());
        }

        @Test
        @DisplayName("only the concepts on an authority's own namespace are asked of it")
        void conceptsArePartitionedByAuthority() {
            givenHarvestedLabels(List.of());
            givenRetrieverReturns(skosFor(NVS_CONCEPT, "sea water temperature", "A measurement.", NVS + "x"));

            service.graphs(Set.of(
                NVS_CONCEPT,
                AGROVOC + "c_8543",
                "https://catalogue.ceh.ac.uk/id/some-record"
            ));

            verify(retriever).describe(
                argThat(uris -> uris.size() == 1 && uris.contains(NVS_CONCEPT)),
                eq(SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION));
            verify(retriever).describe(
                argThat(uris -> uris.size() == 1 && uris.contains(AGROVOC + "c_8543")),
                eq(SkosConceptRetriever.Retrieval.CONTENT_NEGOTIATION));
        }

        @Test
        @DisplayName("NVS and AGROVOC gain a graph they never had, since neither is harvested")
        void previouslyUndescribedVocabulariesGetAGraph() {
            givenHarvestedLabels(List.of());
            givenRetrieverReturns(skosFor(NVS_CONCEPT, "sea water temperature", "A measurement.", NVS + "x"));

            val graphs = service.graphs(Set.of(NVS_CONCEPT));

            assertThat(graphs.keySet(), hasItem(NVS));
            val model = parse(graphs.get(NVS));
            assertTrue(model.contains(createResource(NVS_CONCEPT), SKOS.definition, "A measurement."));
        }
    }

    @Nested
    @DisplayName("When an authority cannot be reached")
    class RetrievalFailure {

        @Test
        @DisplayName("a total retrieval failure leaves the graph alone rather than replacing it with less")
        void totalFailureLeavesTheGraphAlone() {
            // CAST has harvested labels, so there would be something to publish.
            // Publishing it would replace a graph holding definitions and hierarchy
            // with one holding only labels, which is worse than not writing at all.
            givenHarvestedLabels(List.of(new Keyword("nitrogen", "cast", CAST_NITROGEN)));
            givenRetrieverReturns(ModelFactory.createDefaultModel());

            assertThat(service.graphs(Set.of(CAST_NITROGEN)).keySet(), not(hasItem(CAST)));
        }

        @Test
        @DisplayName("a partial retrieval still publishes, since what is missing is what no run could get")
        void partialRetrievalStillPublishes() {
            // This was once a compromise -- "some description beats none" -- and
            // is now simply correct: the retriever falls back to a stored copy
            // per concept, so a concept still absent from its result is one that
            // has never been retrieved and that a later run cannot help either.
            // Holding the graph back for it would hold it back for good.
            givenHarvestedLabels(List.of());
            givenRetrieverReturns(skosFor(NVS_CONCEPT, "sea water temperature", "A measurement.", NVS + "x"));

            val graphs = service.graphs(Set.of(NVS_CONCEPT, NVS + "collection/P07/current/MISSING/"));

            assertThat(graphs.keySet(), hasItem(NVS));
        }

        @Test
        @DisplayName("an unreadable keyword index does not stop a vocabulary that is not harvested")
        void solrFailureDoesNotBlockRetrieval() throws Exception {
            // NVS is retrieval-only, so its graph never held harvested labels
            // and losing them costs it nothing.
            given(solrClient.query(anyString(), any(SolrQuery.class), eq(POST)))
                .willThrow(new RuntimeException("connection refused"));
            givenRetrieverReturns(skosFor(NVS_CONCEPT, "sea water temperature", "A measurement.", NVS + "x"));

            assertThat(service.graphs(Set.of(NVS_CONCEPT)).keySet(), hasItem(NVS));
        }

        @Test
        @DisplayName("but it does hold back a harvested vocabulary, whose graph is mostly labels")
        void solrFailureHoldsBackAHarvestedVocabulary() throws Exception {
            given(solrClient.query(anyString(), any(SolrQuery.class), eq(POST)))
                .willThrow(new RuntimeException("connection refused"));
            givenRetrieverReturns(skosFor(CAST_NITROGEN, "nitrogen", "An element.", CAST + "1"));

            assertThat(
                "publishing CAST from retrieval alone would drop its harvested labels",
                service.graphs(Set.of(CAST_NITROGEN)).keySet(), not(hasItem(CAST))
            );
        }

        @Test
        @DisplayName("a harvest that silently returned nothing is treated as a fault, not an empty vocabulary")
        void emptyHarvestIsTreatedAsAFault() {
            // Solr is perfectly healthy here and simply holds no CAST labels.
            // dri-one #349 found two vocabularies harvesting into silence, and
            // the endpoint would have published that silence as fact.
            givenHarvestedLabels(List.of(new Keyword("river", "gemet", GEMET + "1000")));
            givenRetrieverReturns(skosFor(CAST_NITROGEN, "nitrogen", "An element.", CAST + "1"));

            assertThat(service.graphs(Set.of(CAST_NITROGEN)).keySet(), not(hasItem(CAST)));
        }
    }

    @Nested
    @DisplayName("What every graph carries")
    class GraphContents {

        @Test
        @DisplayName("the graph describes itself, and claims no licence")
        void provenanceButNoLicence() {
            givenHarvestedLabels(List.of(new Keyword("A horizon", "envThes", ENVTHES + "41")));

            val model = parse(service.graphs(Set.of()).get(ENVTHES));
            val graph = createResource(ENVTHES);

            assertThat(
                model.listObjectsOfProperty(graph, createProperty("http://www.w3.org/ns/prov#generatedAtTime"))
                    .next().asLiteral().getString(),
                is("2026-09-02T12:00:00Z")
            );
            assertTrue(
                model.listObjectsOfProperty(createProperty("http://purl.org/dc/terms/license")).toList().isEmpty(),
                "claiming the wrong licence would be worse than claiming none"
            );
        }

        @Test
        @DisplayName("an awkward label is serialised by Jena, so it round-trips")
        void awkwardLabelsRoundTrip() {
            givenHarvestedLabels(List.of(
                new Keyword("ICA\\R1\\180100", "envThes", ENVTHES + "1"),
                new Keyword("the \"quoted\" term", "envThes", ENVTHES + "2")
            ));

            // Serialising through Jena rather than string concatenation is what
            // makes this safe; a hand-built literal took the export down for a
            // week (dri-one #344).
            val model = parse(service.graphs(Set.of()).get(ENVTHES));

            assertTrue(model.contains(createResource(ENVTHES + "1"), SKOS.prefLabel, "ICA\\R1\\180100"));
            assertTrue(model.contains(createResource(ENVTHES + "2"), SKOS.prefLabel, "the \"quoted\" term"),
                "a quote survives intact now, rather than being replaced with an apostrophe");
        }

        @Test
        @DisplayName("a concept URI is normalised, so it matches the node the catalogue graph uses")
        void conceptUrisAreNormalised() {
            givenHarvestedLabels(List.of(
                new Keyword("A horizon", "envThes", "https://vocabs.lter-europe.net/EnvThes/41")
            ));

            val model = parse(service.graphs(Set.of()).get(ENVTHES));
            assertTrue(
                model.containsResource(createResource(ENVTHES + "41")),
                "EnvThes mints http; an https label would never join the catalogue's node"
            );
        }
    }

    @Nested
    @DisplayName("Declaring what the endpoint offers")
    class Declaration {

        @Test
        @DisplayName("the declaration is independent of what there is to publish today")
        void declarationIsIndependentOfContent() {
            givenHarvestedLabels(List.of());

            assertThat(
                service.sourceGraphs().stream().map(SourceGraphProvider.SourceGraph::graph).toList(),
                containsInAnyOrder(GEMET, ENVTHES, CAST, NVS, AGROVOC,
                    "https://digital.ceh.ac.uk/vocab/ra/", "https://digital.ceh.ac.uk/vocab/fdri/")
            );
        }

        @Test
        @DisplayName("every declared graph has a title, so the VoID description is readable")
        void everyGraphHasATitle() {
            assertTrue(
                service.sourceGraphs().stream()
                    .noneMatch(a -> a.title() == null || a.title().isBlank()),
                "an unlabelled dataset in a discovery document tells a consumer nothing"
            );
        }
    }

    private static <T> T argThat(java.util.function.Predicate<java.util.Collection<String>> predicate) {
        return org.mockito.ArgumentMatchers.argThat(predicate::test);
    }
}
