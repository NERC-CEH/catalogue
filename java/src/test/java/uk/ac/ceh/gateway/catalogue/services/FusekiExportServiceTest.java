package uk.ac.ceh.gateway.catalogue.services;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClientResponseException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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
    private VoidStatsService voidStatsService;
    private MockRestServiceServer mockServer;

    private static final String BASE_URI = "http://catalogue.invalid/";
    private static final List<String> FUSEKI_CATALOGUE_IDS = List.of("eidc", "ukeof");
    private static final String FUSEKI_DATASET_URL = "http://fuseki.invalid/";
    private static final String FUSEKI_USERNAME = "username";
    private static final String FUSEKI_PASSWORD = "password";

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
            List.of(vocabularyGraphService)
        );
    }

    @Test
    @SneakyThrows
    void exportDocuments() {
        // given
        given(documentsToTurtleService.getBigTtl(any()))
            .willReturn(Optional.of("ttl"));
        given(metadataListingService.getPublicDocumentsOfCatalogue(anyString()))
            .willReturn(List.of());

        mockServer
            .expect(requestTo(equalTo(FUSEKI_DATASET_URL + "?graph=" + BASE_URI)))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(header(HttpHeaders.CONTENT_TYPE, "text/turtle;charset=UTF-8"))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic dXNlcm5hbWU6cGFzc3dvcmQ="))
            .andExpect(content().string(equalTo("ttl\nttl")))
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
        given(documentsToTurtleService.getBigTtl(any())).willReturn(Optional.of("ttl"));
        given(metadataListingService.getPublicDocumentsOfCatalogue(anyString())).willReturn(List.of());
        given(vocabularyGraphService.graphs(any())).willReturn(new LinkedHashMap<>(Map.of(
            GEMET_GRAPH, "gemet-ttl"
        )));

        mockServer.expect(requestTo(equalTo(FUSEKI_DATASET_URL + "?graph=" + BASE_URI)))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(content().string(equalTo("ttl\nttl")))
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
    @DisplayName("one vocabulary graph failing stops neither the others nor the export")
    void oneVocabularyGraphFailingDoesNotStopTheRest() {
        given(documentsToTurtleService.getBigTtl(any())).willReturn(Optional.of("ttl"));
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
        given(documentsToTurtleService.getBigTtl(any())).willReturn(Optional.of("ttl"));
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
            .willReturn(Optional.of("ttl"));
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
            .willReturn(Optional.of("ttl"));
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
            .willReturn(Optional.of("ttl"));
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
