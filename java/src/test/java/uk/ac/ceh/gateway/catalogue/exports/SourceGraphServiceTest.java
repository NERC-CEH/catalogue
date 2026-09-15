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
import java.util.List;
import java.util.Set;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Publishing one named graph per authority.
 *
 * <p>This was three test classes — one per phase of dri-one #350 — totalling 833
 * lines, and they asserted the same policy three times against different data.
 * The policy is asserted once here, deliberately across more than one source, so
 * that "one authority failing does not stop another" is a statement about the
 * service rather than a coincidence of which phase you are reading.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Publishing what each authority says")
class SourceGraphServiceTest {

    private static final String ORCID_GRAPH = "https://orcid.org/";
    private static final String DOI_GRAPH = "https://doi.org/";
    private static final String CLAIRE = ORCID_GRAPH + "0000-0002-0394-2998";
    private static final String PAPER = DOI_GRAPH + "10.1016/j.example.2020.1";
    private static final String VOID = "http://rdfs.org/ns/void#";

    @Mock private AuthorityRetriever retriever;

    private final OrcidSource orcid = new OrcidSource(500);
    private final DoiSource doi = new DoiSource();
    private final CastSource vocabulary = new CastSource("https://vocabs.ceh.ac.uk/sparql");

    private SourceGraphProgress graphProgress;
    private SourceGraphService service;

    @BeforeEach
    void setUp() {
        graphProgress = new SourceGraphProgress();
        service = new SourceGraphService(List.of(orcid, doi, vocabulary), retriever,
            graphProgress, Clock.fixed(Instant.parse("2026-09-07T12:00:00Z"), ZoneOffset.UTC));
    }

    private static AuthorityRetriever.Descriptions complete(Model model) {
        return new AuthorityRetriever.Descriptions(model, 0, 0);
    }

    private static AuthorityRetriever.Descriptions stillFilling(Model model, int deferred) {
        return new AuthorityRetriever.Descriptions(model, deferred, 0);
    }

    private static AuthorityRetriever.Descriptions withFailures(Model model, int failures) {
        return new AuthorityRetriever.Descriptions(model, 0, failures);
    }

    private static Model labelled(String uri, String label) {
        val model = ModelFactory.createDefaultModel();
        model.add(model.getResource(uri), RDFS.label, label);
        return model;
    }

    private static Model parse(String turtle) {
        val model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new StringReader(turtle), null, Lang.TURTLE);
        return model;
    }

    @Nested
    @DisplayName("Which entities each authority is asked about")
    class Partitioning {

        @Test
        @DisplayName("each is asked only about the IRIs it claims")
        void partitionedBySource() {
            given(retriever.describe(any(), any())).willReturn(complete(labelled(CLAIRE, "Claire Wood")));

            service.graphs(Set.of(CLAIRE, PAPER, "https://catalogue.ceh.ac.uk/id/a-record"));

            verify(retriever).describe(argThat(iris -> iris.equals(List.of(CLAIRE))), eq(orcid));
            verify(retriever).describe(argThat(iris -> iris.equals(List.of(PAPER))), eq(doi));
        }

        @Test
        @DisplayName("an authority with nothing referenced is not asked at all")
        void nothingReferencedForOne() {
            given(retriever.describe(any(), any())).willReturn(complete(labelled(CLAIRE, "Claire Wood")));

            service.graphs(Set.of(CLAIRE));

            verify(retriever, never()).describe(any(), eq(doi));
        }

        @Test
        @DisplayName("nothing referenced at all means no requests and no graphs")
        void nothingReferenced() {
            assertThat(service.graphs(Set.of()), is(java.util.Map.of()));
            verify(retriever, never()).describe(any(), any());
        }

        @Test
        @DisplayName("a vocabulary is left to the service that merges its harvested labels")
        void vocabulariesAreNotOurs() {
            // Two sources feed each vocabulary graph -- the keyword harvest's
            // labels and the retrieved SKOS -- and the export's PUT replaces a
            // graph rather than adding to it, so two writers aiming at the same
            // graph name would mean the second silently replacing the first.
            given(retriever.describe(any(), any())).willReturn(complete(labelled(CLAIRE, "x")));

            service.graphs(Set.of(CLAIRE, "http://onto.nerc.ac.uk/CAST/273"));

            verify(retriever, never()).describe(any(), eq(vocabulary));
            assertThat(service.sourceGraphs().stream().map(SourceGraphProvider.SourceGraph::graph).toList(),
                not(hasItem("http://onto.nerc.ac.uk/CAST/")));
        }
    }

    @Nested
    @DisplayName("What each graph carries")
    class Content {

        @Test
        @DisplayName("descriptions go in the authority's graph, not the catalogue's")
        void inTheAuthoritysGraph() {
            given(retriever.describe(any(), eq(orcid)))
                .willReturn(complete(labelled(CLAIRE, "Claire Wood")));

            val graphs = service.graphs(Set.of(CLAIRE));

            assertThat(graphs, hasKey(ORCID_GRAPH));
            assertTrue(parse(graphs.get(ORCID_GRAPH))
                .contains(createResource(CLAIRE), RDFS.label, "Claire Wood"));
        }

        @Test
        @DisplayName("the header written into the graph says what the declaration says")
        void headerMatchesTheDeclaration() {
            given(retriever.describe(any(), eq(orcid)))
                .willReturn(complete(labelled(CLAIRE, "Claire Wood")));

            val model = parse(service.graphs(Set.of(CLAIRE)).get(ORCID_GRAPH));
            val graph = createResource(ORCID_GRAPH);
            val declared = service.sourceGraphs().stream()
                .filter(candidate -> candidate.graph().equals(ORCID_GRAPH))
                .findFirst().orElseThrow();

            assertThat(model.listObjectsOfProperty(graph, DCTerms.title).toList().stream()
                .map(node -> node.asLiteral().getString()).toList(), contains(declared.title()));
            assertThat(model.listObjectsOfProperty(graph, DCTerms.description).toList().stream()
                .map(node -> node.asLiteral().getString()).toList(), contains(declared.description()));
            assertThat(model.listObjectsOfProperty(graph, createProperty(VOID + "vocabulary"))
                    .toList().stream().map(node -> node.asResource().getURI()).toList(),
                containsInAnyOrder(declared.vocabularies().toArray()));
        }

        @Test
        @DisplayName("a licence is claimed only where the authority's terms are established")
        void licenceOnlyWhenKnown() {
            given(retriever.describe(any(), eq(orcid)))
                .willReturn(complete(labelled(CLAIRE, "Claire Wood")));
            given(retriever.describe(any(), eq(doi)))
                .willReturn(complete(labelled(PAPER, "A paper")));

            val graphs = service.graphs(Set.of(CLAIRE, PAPER));

            assertThat("ORCID releases its public records under CC0",
                parse(graphs.get(ORCID_GRAPH))
                    .listObjectsOfProperty(createResource(ORCID_GRAPH), DCTerms.license).toList()
                    .stream().map(node -> node.asResource().getURI()).toList(),
                contains("https://creativecommons.org/publicdomain/zero/1.0/"));
            assertThat("Crossref's are not established, and the wrong claim is worse than none",
                parse(graphs.get(DOI_GRAPH))
                    .listObjectsOfProperty(createResource(DOI_GRAPH), DCTerms.license).toList(),
                is(empty()));
        }

        @Test
        @DisplayName("the graph records when the copy was taken")
        void provenance() {
            given(retriever.describe(any(), eq(orcid)))
                .willReturn(complete(labelled(CLAIRE, "Claire Wood")));

            val model = parse(service.graphs(Set.of(CLAIRE)).get(ORCID_GRAPH));

            assertThat(model.listObjectsOfProperty(createResource(ORCID_GRAPH),
                    createProperty("http://www.w3.org/ns/prov#generatedAtTime"))
                    .toList().stream().map(node -> node.asLiteral().getString()).toList(),
                contains("2026-09-07T12:00:00Z"));
        }

        @Test
        @DisplayName("the prefixes are the ones the source declared, not a hand-kept list")
        void prefixesComeFromTheDeclaration() {
            // Three services each kept their own prefix map. They are now
            // derived from vocabularies(), so a source that starts emitting a
            // new vocabulary gets its prefix by saying so once.
            given(retriever.describe(any(), eq(orcid)))
                .willReturn(complete(labelled(CLAIRE, "Claire Wood")));

            val turtle = service.graphs(Set.of(CLAIRE)).get(ORCID_GRAPH);

            assertThat(turtle, containsString("PREFIX foaf:"));
            assertThat("and the ones addProvenance always writes",
                turtle, containsString("PREFIX void:"));
            assertThat("but not one this graph has no use for",
                turtle, not(containsString("PREFIX gn:")));
        }
    }

    @Nested
    @DisplayName("When a run is not the best that could be had")
    class Withheld {

        @Test
        @DisplayName("nothing retrieved leaves the graph alone rather than emptying it")
        void nothingRetrieved() {
            given(retriever.describe(any(), eq(orcid)))
                .willReturn(complete(ModelFactory.createDefaultModel()));

            assertThat("publishing an empty graph would replace what is there with less",
                service.graphs(Set.of(CLAIRE)), is(java.util.Map.of()));
        }

        @Test
        @DisplayName("a graph is not replaced with part of itself")
        void notReplacedWhileFilling() {
            given(retriever.describe(any(), eq(orcid)))
                .willReturn(stillFilling(labelled(CLAIRE, "Claire Wood"), 300));

            assertThat(service.graphs(Set.of(CLAIRE)), is(java.util.Map.of()));
        }

        @Test
        @DisplayName("nor when the authority could not serve some of them")
        void notReplacedAfterFailures() {
            // The second count, originally missed: an entity the authority
            // failed to serve is just as absent as one the budget never reached.
            given(retriever.describe(any(), eq(orcid)))
                .willReturn(withFailures(labelled(CLAIRE, "Claire Wood"), 4));

            assertThat(service.graphs(Set.of(CLAIRE)), is(java.util.Map.of()));
        }

        @Test
        @DisplayName("a complete run publishes even if it describes fewer entities than before")
        void completeRunPublishes() {
            given(retriever.describe(any(), eq(orcid)))
                .willReturn(complete(labelled(CLAIRE, "Claire Wood")));

            assertThat(service.graphs(Set.of(CLAIRE)), hasKey(ORCID_GRAPH));
        }

        @Test
        @DisplayName("one authority holding back does not stop another publishing")
        void oneHoldingBackDoesNotStopTheOthers() {
            // The assertion the three separate classes could only make within a
            // phase. Each graph is written with its own PUT, so an authority
            // that is still filling must not cost an unrelated one its update.
            given(retriever.describe(any(), eq(orcid)))
                .willReturn(stillFilling(labelled(CLAIRE, "Claire Wood"), 300));
            given(retriever.describe(any(), eq(doi)))
                .willReturn(complete(labelled(PAPER, "A paper")));

            val graphs = service.graphs(Set.of(CLAIRE, PAPER));

            assertThat(graphs, hasKey(DOI_GRAPH));
            assertThat(graphs, not(hasKey(ORCID_GRAPH)));
        }
    }

    @Nested
    @DisplayName("Declaring what the endpoint offers")
    class Declaration {

        @Test
        @DisplayName("every source's graph is declared, whether or not it has content today")
        void declarationIsIndependentOfContent() {
            assertThat(service.sourceGraphs().stream()
                    .map(SourceGraphProvider.SourceGraph::graph).toList(),
                containsInAnyOrder(ORCID_GRAPH, DOI_GRAPH));
        }

        @Test
        @DisplayName("each declared graph has a title and a description")
        void everyGraphIsDescribed() {
            assertTrue(service.sourceGraphs().stream().noneMatch(graph ->
                graph.title() == null || graph.title().isBlank()
                    || graph.description() == null || graph.description().isBlank()));
        }
    }
}
