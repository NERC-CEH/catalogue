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
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Publishing what Wikidata says (dri-one #350 phase 5)")
class WikidataGraphServiceTest {

    private static final String GRAPH = "http://www.wikidata.org/entity/";
    private static final String SPECKLED_WOOD = GRAPH + "Q663181";

    @Mock private WikidataRetriever retriever;
    private WikidataGraphService service;

    @BeforeEach
    void setUp() {
        service = new WikidataGraphService(
            retriever, Clock.fixed(Instant.parse("2026-09-03T13:00:00Z"), ZoneOffset.UTC));
    }

    private static Model labelled(String uri, String label) {
        val model = ModelFactory.createDefaultModel();
        model.add(model.getResource(uri), RDFS.label, label, "en");
        return model;
    }

    private static WikidataRetriever.Descriptions complete(Model model) {
        return new WikidataRetriever.Descriptions(model, 0, 0);
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
        @DisplayName("only Wikidata entities, and only well-formed ones")
        void onlyWikidataEntities() {
            given(retriever.describe(any())).willReturn(complete(labelled(SPECKLED_WOOD, "Speckled Wood")));

            service.graphs(Set.of(
                SPECKLED_WOOD,
                GRAPH + "(Q1054552",
                "https://orcid.org/0000-0002-0394-2998",
                "https://catalogue.ceh.ac.uk/id/x"));

            verify(retriever).describe(argThat(iris ->
                iris.size() == 1 && iris.contains(SPECKLED_WOOD)));
        }

        @Test
        @DisplayName("nothing referenced means no request and no graph")
        void nothingReferenced() {
            assertThat(service.graphs(Set.of()), is(Map.of()));
            verify(retriever, never()).describe(any());
        }
    }

    @Nested
    @DisplayName("What the graph carries")
    class GraphContents {

        @Test
        @DisplayName("descriptions go in Wikidata's graph, not the catalogue's")
        void graphIsWikidatas() {
            given(retriever.describe(any())).willReturn(complete(labelled(SPECKLED_WOOD, "Speckled Wood")));

            val graphs = service.graphs(Set.of(SPECKLED_WOOD));

            assertThat(graphs.keySet(), contains(GRAPH));
            assertTrue(parse(graphs.get(GRAPH))
                .contains(createResource(SPECKLED_WOOD), RDFS.label, "Speckled Wood", "en"));
        }

        @Test
        @DisplayName("CC0 is claimed, because Wikidata's terms are not in doubt")
        void licenceIsAsserted() {
            given(retriever.describe(any())).willReturn(complete(labelled(SPECKLED_WOOD, "Speckled Wood")));

            val model = parse(service.graphs(Set.of(SPECKLED_WOOD)).get(GRAPH));

            assertTrue(model.contains(createResource(GRAPH), DCTerms.license,
                createResource("https://creativecommons.org/publicdomain/zero/1.0/")));
        }

        @Test
        @DisplayName("the graph records when the copy was taken")
        void provenance() {
            given(retriever.describe(any())).willReturn(complete(labelled(SPECKLED_WOOD, "Speckled Wood")));

            val model = parse(service.graphs(Set.of(SPECKLED_WOOD)).get(GRAPH));

            assertThat(
                model.listObjectsOfProperty(createResource(GRAPH),
                        createProperty("http://www.w3.org/ns/prov#generatedAtTime"))
                    .next().asLiteral().getString(),
                is("2026-09-03T13:00:00Z")
            );
        }
    }

    @Nested
    @DisplayName("When a run is not the best that could be had")
    class PartialRuns {

        @Test
        @DisplayName("nothing retrieved leaves the graph alone rather than emptying it")
        void nothingRetrieved() {
            given(retriever.describe(any()))
                .willReturn(complete(ModelFactory.createDefaultModel()));

            assertThat(service.graphs(Set.of(SPECKLED_WOOD)).keySet(), not(hasItem(GRAPH)));
        }

        @Test
        @DisplayName("a graph is not replaced with part of itself")
        void partialRunIsNotPublished() {
            given(retriever.describe(any())).willReturn(
                new WikidataRetriever.Descriptions(labelled(SPECKLED_WOOD, "Speckled Wood"), 1500, 0));

            assertThat(
                "one failed batch is 500 entities, so publishing now would drop them all",
                service.graphs(Set.of(SPECKLED_WOOD)).keySet(), not(hasItem(GRAPH))
            );
        }

        @Test
        @DisplayName("nor when a batch could not be served")
        void transientFailuresAlsoHoldItBack() {
            given(retriever.describe(any())).willReturn(
                new WikidataRetriever.Descriptions(labelled(SPECKLED_WOOD, "Speckled Wood"), 0, 500));

            assertThat(service.graphs(Set.of(SPECKLED_WOOD)).keySet(), not(hasItem(GRAPH)));
        }
    }

    @Nested
    @DisplayName("Declaring what the endpoint offers")
    class Declaration {

        @Test
        @DisplayName("the graph is declared whether or not there is anything in it today")
        void declarationIsIndependentOfContent() {
            assertThat(
                service.sourceGraphs().stream()
                    .map(SourceGraphProvider.SourceGraph::graph).toList(),
                contains(GRAPH)
            );
        }

        @Test
        @DisplayName("the declared graph has a title")
        void graphHasATitle() {
            assertTrue(service.sourceGraphs().stream()
                .noneMatch(graph -> graph.title() == null || graph.title().isBlank()));
        }
    }
}
