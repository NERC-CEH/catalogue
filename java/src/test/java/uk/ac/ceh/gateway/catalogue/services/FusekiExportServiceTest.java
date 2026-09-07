package uk.ac.ceh.gateway.catalogue.services;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClientResponseException;
import uk.ac.ceh.gateway.catalogue.exports.DescriptionCache;
import uk.ac.ceh.gateway.catalogue.exports.DocumentsToTurtleService;
import uk.ac.ceh.gateway.catalogue.exports.SourceGraphProvider;
import uk.ac.ceh.gateway.catalogue.wellknown.VoidStats;
import uk.ac.ceh.gateway.catalogue.wellknown.VoidStatsService;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
public class FusekiExportServiceTest {
    private FusekiExportService service;
    @Mock private DocumentsToTurtleService documentsToTurtleService;
    @Mock private MetadataListingService metadataListingService;
    @Mock private SourceGraphProvider vocabularyGraphService;
    @Mock private DescriptionCache descriptionCache;
    private VoidStatsService voidStatsService;
    private MockRestServiceServer mockServer;

    private static final String BASE_URI = "http://catalogue.invalid/";
    private static final List<String> FUSEKI_CATALOGUE_IDS = List.of("eidc", "ukeof");
    private static final String FUSEKI_DATASET_URL = "http://fuseki.invalid/";
    private static final String FUSEKI_USERNAME = "username";
    private static final String FUSEKI_PASSWORD = "password";

    /**
     * Parseable Turtle, which the string {@code "ttl"} these tests used to stub
     * is not. That mattered more than it looks: with an unparseable payload the
     * referenced-IRI set came out empty in every test, so every assertion about
     * the source graphs was made against {@code Set.of()} and the parse-failure
     * branch was taken in all of them without any test saying so.
     *
     * <p>One subject and two objects, one of which is a concept an authority
     * would describe and one of which is a record of our own.
     */
    private static final String TTL = """
        @prefix dcterms: <http://purl.org/dc/terms/> .
        @prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
        <http://catalogue.invalid/id/a-record>
            rdf:type <http://www.w3.org/ns/dcat#Dataset> ;
            dcterms:subject <http://www.eionet.europa.eu/gemet/concept/1> ;
            dcterms:relation <http://catalogue.invalid/id/another-record> .
        """;

    private static final String CONCEPT = "http://www.eionet.europa.eu/gemet/concept/1";

    private final Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);

    @SneakyThrows
    @BeforeEach
    void setup() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        configuration.setDirectoryForTemplateLoading(new File("../templates"));
        voidStatsService = new VoidStatsService();

        service = new FusekiExportService(
            documentsToTurtleService,
            restTemplate,
            BASE_URI,
            FUSEKI_CATALOGUE_IDS,
            FUSEKI_DATASET_URL,
            FUSEKI_USERNAME,
            FUSEKI_PASSWORD,
            voidStatsService,
            metadataListingService,
            List.of(vocabularyGraphService),
            descriptionCache
        );
    }

    @Test
    @SneakyThrows
    void exportDocuments() {
        // given
        given(documentsToTurtleService.getBigTtl(any()))
            .willReturn(Optional.of(TTL));
        given(metadataListingService.getPublicDocumentsOfCatalogue(anyString()))
            .willReturn(List.of());

        mockServer
            .expect(requestTo(equalTo(FUSEKI_DATASET_URL + "?graph=" + BASE_URI)))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(header(HttpHeaders.CONTENT_TYPE, "text/turtle;charset=UTF-8"))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic dXNlcm5hbWU6cGFzc3dvcmQ="))
            .andExpect(content().string(equalTo(TTL + "\n" + TTL)))
            .andRespond(withSuccess());

        // when
        service.runExport();

        // then
        mockServer.verify();
        verify(documentsToTurtleService).getBigTtl(FUSEKI_CATALOGUE_IDS.get(0));
        verify(documentsToTurtleService).getBigTtl(FUSEKI_CATALOGUE_IDS.get(1));
    }

    @Test
    @SneakyThrows
    void doNotExportDocuments() {
        // given
        given(documentsToTurtleService.getBigTtl(any()))
            .willReturn(Optional.empty());

        // when
        service.runExport();

        // then
        mockServer.verify();
        verify(documentsToTurtleService).getBigTtl(FUSEKI_CATALOGUE_IDS.get(0));
        verify(documentsToTurtleService).getBigTtl(FUSEKI_CATALOGUE_IDS.get(1));
    }


    private static final String GEMET_GRAPH = "http://www.eionet.europa.eu/gemet/";
    private static final String ENVTHES_GRAPH = "http://vocabs.lter-europe.net/EnvThes/";

    @Test
    @SneakyThrows
    @DisplayName("each vocabulary's labels go to their own graph, alongside the catalogue's")
    void exportsVocabularyGraphsSeparately() {
        given(documentsToTurtleService.getBigTtl(any())).willReturn(Optional.of(TTL));
        given(metadataListingService.getPublicDocumentsOfCatalogue(anyString())).willReturn(List.of());
        given(vocabularyGraphService.graphs(any())).willReturn(new LinkedHashMap<>(Map.of(
            GEMET_GRAPH, "gemet-ttl"
        )));

        mockServer.expect(requestTo(equalTo(FUSEKI_DATASET_URL + "?graph=" + BASE_URI)))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(content().string(equalTo(TTL + "\n" + TTL)))
            .andRespond(withSuccess());
        mockServer.expect(requestTo(equalTo(FUSEKI_DATASET_URL + "?graph=" + GEMET_GRAPH)))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(content().string(equalTo("gemet-ttl")))
            .andRespond(withSuccess());

        service.runExport();

        mockServer.verify();
    }

    @Test
    @SneakyThrows
    @DisplayName("the description cache snapshot is written once for the whole run, not per authority")
    void writesTheCacheSnapshotOncePerRun() {
        given(documentsToTurtleService.getBigTtl(any())).willReturn(Optional.of(TTL));
        given(metadataListingService.getPublicDocumentsOfCatalogue(anyString())).willReturn(List.of());
        given(vocabularyGraphService.graphs(any())).willReturn(new LinkedHashMap<>(Map.of(
            GEMET_GRAPH, "gemet-ttl"
        )));

        mockServer.expect(requestTo(equalTo(FUSEKI_DATASET_URL + "?graph=" + BASE_URI)))
            .andExpect(method(HttpMethod.PUT)).andRespond(withSuccess());
        mockServer.expect(requestTo(equalTo(FUSEKI_DATASET_URL + "?graph=" + GEMET_GRAPH)))
            .andExpect(method(HttpMethod.PUT)).andRespond(withSuccess());

        service.runExport();

        // Each retriever used to call save() whenever it had fetched anything,
        // which is one whole-file rewrite per authority over a CIFS share: ten
        // of them across phases 2 to 5.
        verify(descriptionCache, times(1)).save();
    }

    @Test
    @SneakyThrows
    @DisplayName("a provider throwing still leaves the run's fetches in the snapshot")
    void writesTheSnapshotEvenWhenAProviderThrows() {
        given(documentsToTurtleService.getBigTtl(any())).willReturn(Optional.of(TTL));
        given(metadataListingService.getPublicDocumentsOfCatalogue(anyString())).willReturn(List.of());
        given(vocabularyGraphService.graphs(any()))
            .willThrow(new IllegalStateException("an authority went away mid-run"));

        mockServer.expect(requestTo(equalTo(FUSEKI_DATASET_URL + "?graph=" + BASE_URI)))
            .andExpect(method(HttpMethod.PUT)).andRespond(withSuccess());

        service.runExport();

        verify(descriptionCache, times(1)).save();
    }

    @Test
    @SneakyThrows
    @DisplayName("a source graph is asked about the IRIs the catalogue cites, not the records citing them")
    void sourceGraphsSeeTheReferencedIris() {
        given(documentsToTurtleService.getBigTtl(any())).willReturn(Optional.of(TTL));
        given(metadataListingService.getPublicDocumentsOfCatalogue(anyString())).willReturn(List.of());
        given(vocabularyGraphService.graphs(any())).willReturn(Map.of());

        mockServer.expect(requestTo(equalTo(FUSEKI_DATASET_URL + "?graph=" + BASE_URI)))
            .andExpect(method(HttpMethod.PUT)).andRespond(withSuccess());

        service.runExport();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
        verify(vocabularyGraphService).graphs(captor.capture());
        val referenced = captor.getValue();

        assertThat("a concept the record cites is what an authority can describe",
            referenced, hasItem(CONCEPT));
        assertThat("objects, so a related record counts as referenced",
            referenced, hasItem("http://catalogue.invalid/id/another-record"));
        assertThat("subjects do not: this graph's subjects are our own records and minted nodes",
            referenced, not(hasItem("http://catalogue.invalid/id/a-record")));
    }

    @Test
    @SneakyThrows
    @DisplayName("a catalogue that will not parse holds back every source graph, not just its own")
    void aParseFailureHoldsBackEverySourceGraph() {
        // Both catalogues are exported into one graph, and the source graphs are
        // built from the union of what they cite. So one unparseable catalogue
        // leaves the referenced set short, and every graph would be replaced with
        // one describing fewer entities -- silently, because the providers'
        // completeness guard measures against that already-shortened set.
        given(documentsToTurtleService.getBigTtl("eidc")).willReturn(Optional.of(TTL));
        given(documentsToTurtleService.getBigTtl("ukeof")).willReturn(Optional.of("not turtle {"));
        given(metadataListingService.getPublicDocumentsOfCatalogue(anyString())).willReturn(List.of());

        mockServer.expect(requestTo(equalTo(FUSEKI_DATASET_URL + "?graph=" + BASE_URI)))
            .andExpect(method(HttpMethod.PUT)).andRespond(withSuccess());

        service.runExport();

        // The catalogue's own graph still published: it is posted from the Turtle
        // as it stands and does not depend on parsing it.
        mockServer.verify();
        verify(vocabularyGraphService, never()).graphs(any());
        assertThat("the export still completed", service.getLastExported(), is(notNullValue()));
    }

    @Test
    @SneakyThrows
    @DisplayName("a catalogue that will not parse keeps its previous stats rather than being zeroed")
    void aParseFailureLeavesTheOldStatsAlone() {
        voidStatsService.update("ukeof", new VoidStats(5L, 10L, Map.of()));
        given(documentsToTurtleService.getBigTtl("eidc")).willReturn(Optional.of(TTL));
        given(documentsToTurtleService.getBigTtl("ukeof")).willReturn(Optional.of("not turtle {"));
        given(metadataListingService.getPublicDocumentsOfCatalogue(anyString())).willReturn(List.of());

        mockServer.expect(requestTo(equalTo(FUSEKI_DATASET_URL + "?graph=" + BASE_URI)))
            .andExpect(method(HttpMethod.PUT)).andRespond(withSuccess());

        service.runExport();

        // Zeroes are not a smaller answer, they are a wrong one: a VoID
        // description claiming a dataset holds no triples is worse than
        // yesterday's count.
        assertThat(voidStatsService.get("ukeof").orElseThrow().triples(), is(10L));
        assertThat("the catalogue that did parse is still updated",
            voidStatsService.get("eidc").orElseThrow().triples(), is(3L));
    }

    @Test
    @SneakyThrows
    @DisplayName("one vocabulary graph failing stops neither the others nor the export")
    void oneVocabularyGraphFailingDoesNotStopTheRest() {
        given(documentsToTurtleService.getBigTtl(any())).willReturn(Optional.of(TTL));
        given(metadataListingService.getPublicDocumentsOfCatalogue(anyString())).willReturn(List.of());
        val graphs = new LinkedHashMap<String, String>();
        graphs.put(GEMET_GRAPH, "gemet-ttl");
        graphs.put(ENVTHES_GRAPH, "envthes-ttl");
        given(vocabularyGraphService.graphs(any())).willReturn(graphs);

        mockServer.expect(requestTo(equalTo(FUSEKI_DATASET_URL + "?graph=" + BASE_URI)))
            .andRespond(withSuccess());
        // The first vocabulary graph is rejected, exactly as the catalogue graph was
        // for a week over one bad literal (dri-one #344).
        mockServer.expect(requestTo(equalTo(FUSEKI_DATASET_URL + "?graph=" + GEMET_GRAPH)))
            .andRespond(withServerError());
        mockServer.expect(requestTo(equalTo(FUSEKI_DATASET_URL + "?graph=" + ENVTHES_GRAPH)))
            .andRespond(withSuccess());

        service.runExport();

        mockServer.verify();
        assertThat(
            "the export still completed, so the catalogue graph and the void stats are current",
            service.getLastExported(), is(notNullValue())
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("no vocabulary labels means no vocabulary graph is written at all")
    void noLabelsWritesNoGraph() {
        given(documentsToTurtleService.getBigTtl(any())).willReturn(Optional.of(TTL));
        given(metadataListingService.getPublicDocumentsOfCatalogue(anyString())).willReturn(List.of());
        given(vocabularyGraphService.graphs(any())).willReturn(Map.of());

        mockServer.expect(requestTo(equalTo(FUSEKI_DATASET_URL + "?graph=" + BASE_URI)))
            .andRespond(withSuccess());

        service.runExport();

        // A graph a previous run filled is left as it was, rather than emptied.
        mockServer.verify();
    }

    private static final String EIDC_TTL =
        """
            <http://example.org/d1> a <http://www.w3.org/ns/dcat#Dataset> .
            <http://example.org/d2> a <http://www.w3.org/ns/dcat#Dataset> .
            <http://example.org/p1> a <http://xmlns.com/foaf/0.1/Person> .
            """;

    private static final String UKEOF_TTL =
        "<http://example.org/f1> a <https://digital.ceh.ac.uk/ontology/doo/EnvironmentalMonitoringFacility> .\n";

    @Test
    @SneakyThrows
    void exportUpdatesVoidStats() {
        // given
        given(documentsToTurtleService.getBigTtl("eidc")).willReturn(Optional.of(EIDC_TTL));
        given(documentsToTurtleService.getBigTtl("ukeof")).willReturn(Optional.of(UKEOF_TTL));
        given(metadataListingService.getPublicDocumentsOfCatalogue("eidc"))
            .willReturn(List.of("a", "b", "c"));
        given(metadataListingService.getPublicDocumentsOfCatalogue("ukeof"))
            .willReturn(List.of("x", "y"));

        mockServer
            .expect(requestTo(equalTo(FUSEKI_DATASET_URL + "?graph=" + BASE_URI)))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withSuccess());

        // when
        service.runExport();

        // then
        assertThat(voidStatsService.get("eidc"))
            .isPresent()
            .hasValueSatisfying(s -> {
                assertThat(s.entities()).isEqualTo(3L);
                assertThat(s.triples()).isEqualTo(3L);
                assertThat(s.classEntityCounts()).containsEntry(
                    "http://www.w3.org/ns/dcat#Dataset", 2L
                );
                assertThat(s.classEntityCounts()).containsEntry(
                    "http://xmlns.com/foaf/0.1/Person", 1L
                );
            });
        assertThat(voidStatsService.get("ukeof"))
            .isPresent()
            .hasValueSatisfying(s -> {
                assertThat(s.entities()).isEqualTo(2L);
                assertThat(s.classEntityCounts()).containsEntry(
                    "https://digital.ceh.ac.uk/ontology/doo/EnvironmentalMonitoringFacility", 1L
                );
            });
    }

    @Test
    @SneakyThrows
    @DisplayName("export clears stale VoID stats when catalogue has no TTL")
    void exportClearsStaleVoidStats() {
        // given — ukeof previously had stats but now has no documents
        voidStatsService.update("ukeof", new VoidStats(5L, 10L, Map.of()));
        given(documentsToTurtleService.getBigTtl("eidc")).willReturn(Optional.of(EIDC_TTL));
        given(documentsToTurtleService.getBigTtl("ukeof")).willReturn(Optional.empty());
        given(metadataListingService.getPublicDocumentsOfCatalogue("eidc"))
            .willReturn(List.of("a", "b", "c"));

        mockServer
            .expect(requestTo(equalTo(FUSEKI_DATASET_URL + "?graph=" + BASE_URI)))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withSuccess());

        // when
        service.runExport();

        // then — eidc stats updated, ukeof stats cleared
        assertThat(voidStatsService.get("eidc")).isPresent();
        assertThat(voidStatsService.get("ukeof")).isEmpty();
    }

    /**
     * dri-one #330: {@code CatalogueToTurtleService} prefetches eidc's turtle once a day, separately
     * from this export. Without an explicit refresh, a manual trigger would just republish that stale
     * cached value instead of current data.
     */
    @Test
    @SneakyThrows
    void runExportRefreshesThePrefetchCacheBeforeExporting() {
        // given
        given(documentsToTurtleService.getBigTtl(any()))
            .willReturn(Optional.of(TTL));
        given(metadataListingService.getPublicDocumentsOfCatalogue(anyString()))
            .willReturn(List.of());
        mockServer
            .expect(requestTo(equalTo(FUSEKI_DATASET_URL + "?graph=" + BASE_URI)))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withSuccess());

        // when
        service.runExport();

        // then
        verify(documentsToTurtleService).refresh();
    }

    @Test
    @SneakyThrows
    void tracksWhenTheExportLastCompletedSuccessfully() {
        // given
        assertThat(service.getLastExported()).isNull();
        given(documentsToTurtleService.getBigTtl(any()))
            .willReturn(Optional.of(TTL));
        given(metadataListingService.getPublicDocumentsOfCatalogue(anyString()))
            .willReturn(List.of());
        mockServer
            .expect(requestTo(equalTo(FUSEKI_DATASET_URL + "?graph=" + BASE_URI)))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withSuccess());

        // when
        service.runExport();

        // then
        assertThat(service.getLastExported()).isNotNull();
    }

    @Test
    @SneakyThrows
    void doesNotUpdateLastExportedWhenPostingToFusekiFails() {
        // given
        given(documentsToTurtleService.getBigTtl(any()))
            .willReturn(Optional.of(TTL));
        mockServer
            .expect(requestTo(equalTo(FUSEKI_DATASET_URL + "?graph=" + BASE_URI)))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withServerError());

        // when
        assertThrows(RestClientResponseException.class, service::runExport);

        // then
        assertThat(service.getLastExported()).isNull();
    }
}
