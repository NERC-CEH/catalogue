package uk.ac.ceh.gateway.catalogue.exports;

import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.StringReader;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Publishing what ORCID and ROR say (dri-one #350 phase 3)")
class IdentityGraphServiceTest {

    private static final String ORCID_GRAPH = "https://orcid.org/";
    private static final String ROR_GRAPH = "https://ror.org/";
    private static final String CLAIRE = ORCID_GRAPH + "0000-0002-0394-2998";
    private static final String UKCEH = ROR_GRAPH + "00pggkr55";

    @Mock private IdentityRetriever retriever;
    private IdentityGraphService service;

    @BeforeEach
    void setUp() {
        service = new IdentityGraphService(
            retriever, Clock.fixed(Instant.parse("2026-09-02T12:00:00Z"), ZoneOffset.UTC));
    }

    /** A run that reached every entity it was asked about. */
    private static IdentityRetriever.Descriptions complete(Model model) {
        return new IdentityRetriever.Descriptions(model, 0, 0);
    }

    /** A run that ran out of budget with {@code deferred} entities still to fetch. */
    private static IdentityRetriever.Descriptions stillFilling(Model model, int deferred) {
        return new IdentityRetriever.Descriptions(model, deferred, 0);
    }

    /** A run where the authority could not serve {@code failures} of the entities. */
    private static IdentityRetriever.Descriptions withFailures(Model model, int failures) {
        return new IdentityRetriever.Descriptions(model, 0, failures);
    }

    private static Model personNamed(String uri, String label) {
        val model = ModelFactory.createDefaultModel();
        model.add(model.getResource(uri), RDF.type,
            model.getResource("http://xmlns.com/foaf/0.1/Person"));
        model.add(model.getResource(uri), RDFS.label, label);
        return model;
    }

    private Model parse(String turtle) {
        val model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new StringReader(turtle), null, Lang.TURTLE);
        return model;
    }

    @Nested
    @DisplayName("Which entities are described")
    class Selection {

        @Test
        @DisplayName("only entities the catalogue actually references")
        void onlyReferencedEntities() {
            given(retriever.describe(any(), eq(IdentityRetriever.Authority.ORCID)))
                .willReturn(complete(personNamed(CLAIRE, "Claire Wood")));

            service.graphs(Set.of(CLAIRE, "https://catalogue.ceh.ac.uk/id/some-record"));

            verify(retriever).describe(
                argThat(uris -> uris.size() == 1 && uris.contains(CLAIRE)),
                eq(IdentityRetriever.Authority.ORCID));
        }

        @Test
        @DisplayName("an ORCID's account node is not mistaken for a person")
        void accountNodesAreNotPeople() {
            given(retriever.describe(any(), any())).willReturn(complete(personNamed(CLAIRE, "Claire Wood")));

            service.graphs(Set.of(CLAIRE, CLAIRE + "#orcid-id"));

            verify(retriever).describe(
                argThat(uris -> uris.size() == 1 && !uris.contains(CLAIRE + "#orcid-id")),
                eq(IdentityRetriever.Authority.ORCID));
        }

        @Test
        @DisplayName("an authority with nothing referenced is not asked at all")
        void nothingReferencedMeansNoRequest() {
            assertThat(service.graphs(Set.of()), is(Map.of()));
            verify(retriever, never()).describe(any(), any());
        }
    }

    @Nested
    @DisplayName("What each graph carries")
    class GraphContents {

        @Test
        @DisplayName("the researcher's own name, in ORCID's graph rather than the catalogue's")
        void namesGoInTheAuthoritysGraph() {
            given(retriever.describe(any(), eq(IdentityRetriever.Authority.ORCID)))
                .willReturn(complete(personNamed(CLAIRE, "Claire Wood")));

            val graphs = service.graphs(Set.of(CLAIRE));

            assertThat(graphs.keySet(), hasItem(ORCID_GRAPH));
            assertTrue(
                graphs.keySet().stream().noneMatch(g -> g.contains("catalogue.ceh.ac.uk")),
                "#348 stopped the catalogue asserting names on ORCIDs; this must not put them back"
            );
            assertTrue(parse(graphs.get(ORCID_GRAPH))
                .contains(createResource(CLAIRE), RDFS.label, "Claire Wood"));
        }

        @Test
        @DisplayName("CC0 is claimed, because unlike the vocabularies these terms are known")
        void licenceIsAsserted() {
            given(retriever.describe(any(), eq(IdentityRetriever.Authority.ORCID)))
                .willReturn(complete(personNamed(CLAIRE, "Claire Wood")));

            val model = parse(service.graphs(Set.of(CLAIRE)).get(ORCID_GRAPH));

            assertTrue(
                model.contains(createResource(ORCID_GRAPH), DCTerms.license,
                    createResource("https://creativecommons.org/publicdomain/zero/1.0/")),
                "both ORCID and ROR publish their public records under CC0, so this can be stated"
            );
        }

        @Test
        @DisplayName("the graph records when the copy was taken")
        void provenance() {
            given(retriever.describe(any(), eq(IdentityRetriever.Authority.ORCID)))
                .willReturn(complete(personNamed(CLAIRE, "Claire Wood")));

            val model = parse(service.graphs(Set.of(CLAIRE)).get(ORCID_GRAPH));

            assertThat(
                model.listObjectsOfProperty(createResource(ORCID_GRAPH),
                        createProperty("http://www.w3.org/ns/prov#generatedAtTime"))
                    .next().asLiteral().getString(),
                is("2026-09-02T12:00:00Z")
            );
        }

        @Test
        @DisplayName("people and organisations are kept in their own authorities' graphs")
        void authoritiesAreSeparate() {
            given(retriever.describe(any(), eq(IdentityRetriever.Authority.ORCID)))
                .willReturn(complete(personNamed(CLAIRE, "Claire Wood")));
            given(retriever.describe(any(), eq(IdentityRetriever.Authority.ROR)))
                .willReturn(complete(personNamed(UKCEH, "UK Centre for Ecology & Hydrology")));

            val graphs = service.graphs(Set.of(CLAIRE, UKCEH));

            assertThat(graphs.keySet(), containsInAnyOrder(ORCID_GRAPH, ROR_GRAPH));
            assertTrue(
                parse(graphs.get(ROR_GRAPH)).containsResource(createResource(UKCEH)),
                "an organisation belongs in ROR's graph"
            );
            assertTrue(
                !parse(graphs.get(ORCID_GRAPH)).containsResource(createResource(UKCEH)),
                "and not in ORCID's"
            );
        }
    }

    @Nested
    @DisplayName("When an authority cannot be reached")
    class Failure {

        @Test
        @DisplayName("nothing retrieved leaves the graph alone rather than emptying it")
        void nothingRetrievedLeavesTheGraphAlone() {
            given(retriever.describe(any(), any())).willReturn(complete(ModelFactory.createDefaultModel()));

            assertThat(
                "publishing an empty graph would replace a good one with nothing",
                service.graphs(Set.of(CLAIRE)).keySet(), not(hasItem(ORCID_GRAPH))
            );
        }

        @Test
        @DisplayName("one authority failing does not stop the other publishing")
        void oneAuthorityFailingDoesNotStopTheOther() {
            given(retriever.describe(any(), eq(IdentityRetriever.Authority.ORCID)))
                .willReturn(complete(ModelFactory.createDefaultModel()));
            given(retriever.describe(any(), eq(IdentityRetriever.Authority.ROR)))
                .willReturn(complete(personNamed(UKCEH, "UK Centre for Ecology & Hydrology")));

            val graphs = service.graphs(Set.of(CLAIRE, UKCEH));

            assertThat(graphs.keySet(), containsInAnyOrder(ROR_GRAPH));
        }
    }

    @Nested
    @DisplayName("While the cache is still filling")
    class PartialRuns {

        @Test
        @DisplayName("a graph is not replaced with part of itself")
        void partialRunIsNotPublished() {
            given(retriever.describe(any(), eq(IdentityRetriever.Authority.ORCID)))
                .willReturn(stillFilling(personNamed(CLAIRE, "Claire Wood"), 361));

            assertThat(
                "the export's PUT replaces the graph, so publishing a third of it now "
                    + "would drop the rest until the cache warmed up again",
                service.graphs(Set.of(CLAIRE)).keySet(), not(hasItem(ORCID_GRAPH))
            );
        }

        @Test
        @DisplayName("a graph is not replaced when the authority could not serve some entities")
        void transientFailuresAlsoHoldTheGraphBack() {
            // The hole the review found. These entities are just as absent from
            // this run's model as ones the budget never reached, and a timeout
            // or a rate limit is every bit as likely to succeed tomorrow -- so
            // publishing now drops them from the endpoint.
            given(retriever.describe(any(), eq(IdentityRetriever.Authority.ORCID)))
                .willReturn(withFailures(personNamed(CLAIRE, "Claire Wood"), 400));

            assertThat(
                service.graphs(Set.of(CLAIRE)).keySet(), not(hasItem(ORCID_GRAPH))
            );
        }

        @Test
        @DisplayName("a complete run publishes even if it describes fewer entities than before")
        void aSmallerCompleteRunStillPublishes() {
            // The test is completeness of the run, not size against last time. A
            // catalogue that withdraws records legitimately references fewer
            // ORCIDs, and that reduction must be allowed to reach the endpoint.
            given(retriever.describe(any(), eq(IdentityRetriever.Authority.ORCID)))
                .willReturn(complete(personNamed(CLAIRE, "Claire Wood")));

            assertThat(service.graphs(Set.of(CLAIRE)).keySet(), hasItem(ORCID_GRAPH));
        }

        @Test
        @DisplayName("one authority still filling does not hold back the other")
        void oneAuthorityFillingDoesNotHoldBackTheOther() {
            given(retriever.describe(any(), eq(IdentityRetriever.Authority.ORCID)))
                .willReturn(stillFilling(personNamed(CLAIRE, "Claire Wood"), 500));
            given(retriever.describe(any(), eq(IdentityRetriever.Authority.ROR)))
                .willReturn(complete(personNamed(UKCEH, "UK Centre for Ecology & Hydrology")));

            assertThat(service.graphs(Set.of(CLAIRE, UKCEH)).keySet(),
                containsInAnyOrder(ROR_GRAPH));
        }
    }

    @Nested
    @DisplayName("Declaring what the endpoint offers")
    class Declaration {

        @Test
        @DisplayName("both graphs are declared whether or not there is anything in them today")
        void declarationIsIndependentOfContent() {
            assertThat(
                service.sourceGraphs().stream()
                    .map(SourceGraphProvider.SourceGraph::graph).toList(),
                containsInAnyOrder(ORCID_GRAPH, ROR_GRAPH)
            );
        }

        @Test
        @DisplayName("each declared graph has a title")
        void everyGraphHasATitle() {
            assertTrue(service.sourceGraphs().stream()
                .noneMatch(g -> g.title() == null || g.title().isBlank()));
        }
    }
}
