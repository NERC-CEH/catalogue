package uk.ac.ceh.gateway.catalogue.exports;

import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCTerms;
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
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Publishing what phase 4's authorities say (dri-one #350)")
class ReferenceGraphServiceTest {

    private static final String DOI = "https://doi.org/10.1016/j.example.2020.1";
    private static final String FEATURE = "https://sws.geonames.org/2635167";

    @Mock private ReferenceRetriever retriever;
    private ReferenceGraphService service;
    private final DoiSource doiSource = new DoiSource();
    private final GeoNamesSource geoNamesSource = new GeoNamesSource();

    @BeforeEach
    void setUp() {
        given(retriever.sources()).willReturn(List.of(doiSource, geoNamesSource));
        service = new ReferenceGraphService(
            retriever, Clock.fixed(Instant.parse("2026-09-03T09:00:00Z"), ZoneOffset.UTC));
    }

    private static Model labelled(String uri, String label) {
        val model = ModelFactory.createDefaultModel();
        model.add(model.getResource(uri), RDFS.label, label);
        return model;
    }

    private static ReferenceRetriever.Descriptions complete(Model model) {
        return new ReferenceRetriever.Descriptions(model, 0, 0);
    }

    private static Model parse(String turtle) {
        val model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new StringReader(turtle), null, Lang.TURTLE);
        return model;
    }

    @Nested
    @DisplayName("Which entities are asked about")
    class Selection {

        @Test
        @DisplayName("each authority is asked only about its own IRIs")
        void eachSourceGetsItsOwn() {
            given(retriever.describe(any(), any())).willReturn(complete(labelled(DOI, "A paper")));

            service.graphs(Set.of(DOI, FEATURE, "https://catalogue.ceh.ac.uk/id/x"));

            verify(retriever).describe(
                argThat(iris -> iris.size() == 1 && iris.contains(DOI)), eq(doiSource));
            verify(retriever).describe(
                argThat(iris -> iris.size() == 1 && iris.contains(FEATURE)), eq(geoNamesSource));
        }

        @Test
        @DisplayName("an authority with nothing referenced is not asked at all")
        void unreferencedSourceIsNotAsked() {
            given(retriever.describe(any(), any())).willReturn(complete(labelled(DOI, "A paper")));

            service.graphs(Set.of(DOI));

            verify(retriever, never()).describe(any(), eq(geoNamesSource));
        }

        @Test
        @DisplayName("nothing referenced means no requests and no graphs")
        void nothingReferenced() {
            assertThat(service.graphs(Set.of()), is(Map.of()));
            verify(retriever, never()).describe(any(), any());
        }
    }

    @Nested
    @DisplayName("What each graph carries")
    class GraphContents {

        @Test
        @DisplayName("descriptions go in the authority's graph, not the catalogue's")
        void graphIsTheAuthoritys() {
            given(retriever.describe(any(), eq(doiSource)))
                .willReturn(complete(labelled(DOI, "A paper")));

            val graphs = service.graphs(Set.of(DOI));

            assertThat(graphs.keySet(), hasItem("https://doi.org/"));
            assertTrue(
                graphs.keySet().stream().noneMatch(graph -> graph.contains("catalogue.ceh.ac.uk")),
                "nothing record-derived may be written into a source graph, and nothing "
                    + "from a source graph into the catalogue's"
            );
            assertTrue(parse(graphs.get("https://doi.org/"))
                .contains(createResource(DOI), RDFS.label, "A paper"));
        }

        @Test
        @DisplayName("a licence is claimed only where the authority states one")
        void licenceIsClaimedOnlyWhenKnown() {
            given(retriever.describe(any(), eq(doiSource)))
                .willReturn(complete(labelled(DOI, "A paper")));
            given(retriever.describe(any(), eq(geoNamesSource)))
                .willReturn(complete(labelled(FEATURE, "United Kingdom")));

            val graphs = service.graphs(Set.of(DOI, FEATURE));

            assertTrue(
                parse(graphs.get("https://sws.geonames.org/")).contains(
                    createResource("https://sws.geonames.org/"), DCTerms.license,
                    createResource("https://creativecommons.org/licenses/by/4.0/")),
                "GeoNames declares CC-BY in the payload itself, and it requires attribution"
            );
            assertFalse(
                parse(graphs.get("https://doi.org/"))
                    .contains(createResource("https://doi.org/"), DCTerms.license),
                "the publishers' terms vary and we have not established them; claiming the "
                    + "wrong licence would be worse than claiming none"
            );
        }

        @Test
        @DisplayName("the graph records when the copy was taken")
        void provenance() {
            given(retriever.describe(any(), eq(doiSource)))
                .willReturn(complete(labelled(DOI, "A paper")));

            val model = parse(service.graphs(Set.of(DOI)).get("https://doi.org/"));

            assertThat(
                model.listObjectsOfProperty(createResource("https://doi.org/"),
                        createProperty("http://www.w3.org/ns/prov#generatedAtTime"))
                    .next().asLiteral().getString(),
                is("2026-09-03T09:00:00Z")
            );
        }
    }

    @Nested
    @DisplayName("When a run is not the best that could be had")
    class PartialRuns {

        @Test
        @DisplayName("nothing retrieved leaves the graph alone rather than emptying it")
        void nothingRetrieved() {
            given(retriever.describe(any(), any()))
                .willReturn(complete(ModelFactory.createDefaultModel()));

            assertThat(service.graphs(Set.of(DOI)).keySet(), not(hasItem("https://doi.org/")));
        }

        @Test
        @DisplayName("a graph is not replaced with part of itself")
        void partialRunIsNotPublished() {
            given(retriever.describe(any(), eq(doiSource))).willReturn(
                new ReferenceRetriever.Descriptions(labelled(DOI, "A paper"), 500, 0));

            assertThat(
                "the PUT replaces the graph, so publishing 300 of 882 works now would drop "
                    + "the rest until the cache warmed up again",
                service.graphs(Set.of(DOI)).keySet(), not(hasItem("https://doi.org/"))
            );
        }

        @Test
        @DisplayName("nor when the authority could not serve some of them")
        void transientFailuresAlsoHoldItBack() {
            given(retriever.describe(any(), eq(doiSource))).willReturn(
                new ReferenceRetriever.Descriptions(labelled(DOI, "A paper"), 0, 12));

            assertThat(service.graphs(Set.of(DOI)).keySet(), not(hasItem("https://doi.org/")));
        }

        @Test
        @DisplayName("one authority holding back does not stop the others")
        void oneSourceDoesNotBlockAnother() {
            given(retriever.describe(any(), eq(doiSource))).willReturn(
                new ReferenceRetriever.Descriptions(labelled(DOI, "A paper"), 40, 0));
            given(retriever.describe(any(), eq(geoNamesSource)))
                .willReturn(complete(labelled(FEATURE, "United Kingdom")));

            assertThat(service.graphs(Set.of(DOI, FEATURE)).keySet(),
                containsInAnyOrder("https://sws.geonames.org/"));
        }
    }

    @Nested
    @DisplayName("Declaring what the endpoint offers")
    class Declaration {

        @Test
        @DisplayName("every source's graph is declared, whether or not it has content today")
        void declarationIsIndependentOfContent() {
            assertThat(
                service.sourceGraphs().stream().map(SourceGraphProvider.SourceGraph::graph).toList(),
                containsInAnyOrder("https://doi.org/", "https://sws.geonames.org/")
            );
        }

        @Test
        @DisplayName("each declared graph has a title")
        void everyGraphHasATitle() {
            assertTrue(service.sourceGraphs().stream()
                .noneMatch(graph -> graph.title() == null || graph.title().isBlank()));
        }

        @Test
        @DisplayName("the declaration comes from the same list the publishing does")
        void declarationCannotDriftFromPublishing() {
            // One list, so a source added to the pipeline cannot be missing from
            // the VoID description or vice versa.
            given(retriever.describe(any(), any())).willReturn(complete(labelled(DOI, "A paper")));

            val declared = service.sourceGraphs().stream()
                .map(SourceGraphProvider.SourceGraph::graph).toList();

            assertTrue(declared.containsAll(service.graphs(Set.of(DOI, FEATURE)).keySet()));
        }
    }

    @Nested
    @DisplayName("Refresh ages")
    class RefreshAges {

        @Test
        @DisplayName("a paper is refreshed less often than a curated site record")
        void agesReflectHowFastTheAnswersChange() {
            assertThat(new DoiSource().maxAge(), is(Duration.ofDays(90)));
            assertThat(
                "a monitoring site's record is actively curated; a published paper's is not",
                new DeimsSource().maxAge(), is(Duration.ofDays(30))
            );
        }
    }
}
