package uk.ac.ceh.gateway.catalogue.elter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;
import lombok.val;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.publication.PublicationService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("B2SHAREImportService")
public class B2shareImportServiceTest {
    private B2shareImportService b2shareImportService;
    private String testB2shareResponse;
    private QueryResponse queryResponse;
    private CatalogueUser expectedUser;

    String b2shareResponseUrl = getClass().getResource("b2share-valid-api-response.json").toString();
    String invalidB2shareResponseUrl = getClass().getResource("b2share-invalid-api-response.json").toString();

    private static final String CATALOGUE = "elter";
    private static final String RECORD_ID = "00000000-0000-0000-0000-000000000000";

    @Mock private DocumentRepository documentRepository;
    @Mock private PublicationService publicationService;
    @Mock private SolrClient solrClient;

    @BeforeEach
    void setup() {
        queryResponse = mock(QueryResponse.class);

        expectedUser = new CatalogueUser()
            .setUsername("B2SHARE metadata import")
            .setEmail("info@eudat.eu");
    }

    @Test
    @SneakyThrows
    public void importNewRecord() {
        // setup
        b2shareImportService = new B2shareImportService(
                documentRepository,
                publicationService,
                solrClient,
                b2shareResponseUrl
                );

        // given
        given(solrClient.query(any(String.class), any(SolrParams.class), eq(POST)))
            .willReturn(queryResponse);
        given(queryResponse.getResults())
            .willReturn(new SolrDocumentList());

        given(documentRepository.saveNew(
                    any(CatalogueUser.class),
                    any(ElterDocument.class),
                    any(String.class),
                    any(String.class)
                    ))
            .willReturn(new ElterDocument().setId(RECORD_ID));

        // when
        b2shareImportService.runImport();

        // then
        // check interactions
        ArgumentCaptor<ElterDocument> argument = ArgumentCaptor.forClass(ElterDocument.class);
        verify(documentRepository).saveNew(eq(expectedUser), argument.capture(), eq(CATALOGUE), eq("Create new record 10.23728/b2share.b56cd875765a403599859177fced08ae"));
        verify(publicationService).transition(expectedUser, RECORD_ID, "ykhm7b");
        verify(publicationService).transition(expectedUser, RECORD_ID, "re4vkb");

        // check created document
        ElterDocument createdDocument = argument.getValue();
        assertEquals(
                "TERENO Wüstebach meteorological data",
                createdDocument.getTitle()
                );
        assertEquals(
                "10 minute interval temperature and precipitation in °C and mm from different sensors of a meteorological station.Sensor names are temperature at 2m, Precipitation_Cum10min_OttNRTtotal, Precipitation_Cum10min_OttRTNRT, Precipitation_Cum10min_OttNRT, Precipitation_Cum10min_Ecotech, Precipitation_Cum10min_RainCap",
                createdDocument.getDescription()
                );
        assertEquals(
                "10.23728/b2share.b56cd875765a403599859177fced08ae",
                createdDocument.getImportId()
                );
        assertEquals(
                "signpost",
                createdDocument.getType()
                );
        assertEquals(
                "Level 0",
                createdDocument.getDataLevel()
                );
    }

    @Test
    @SneakyThrows
    public void updateExistingRecord() {
        // setup
        b2shareImportService = new B2shareImportService(
                documentRepository,
                publicationService,
                solrClient,
                b2shareResponseUrl
                );

        Map<String, Object> solrFieldMapping = new HashMap<>();
        solrFieldMapping.put("importId", "10.23728/b2share.b56cd875765a403599859177fced08ae");
        solrFieldMapping.put("identifier", RECORD_ID);

        SolrDocumentList mockResults = new SolrDocumentList();
        mockResults.add(new SolrDocument(solrFieldMapping));

        // given
        given(solrClient.query(any(String.class), any(SolrParams.class), eq(POST)))
            .willReturn(queryResponse);
        given(queryResponse.getResults())
            .willReturn(mockResults);

        given(documentRepository.save(
                    any(CatalogueUser.class),
                    any(ElterDocument.class),
                    any(String.class),
                    any(String.class)
                    ))
            .willReturn(new ElterDocument().setId(RECORD_ID));

        given(documentRepository.read(RECORD_ID))
            .willReturn(new ElterDocument().setId(RECORD_ID));

        // when
        b2shareImportService.runImport();

        // then
        // check interactions
        verify(documentRepository).read(RECORD_ID);
        ArgumentCaptor<ElterDocument> argument = ArgumentCaptor.forClass(ElterDocument.class);
        verify(documentRepository).save(eq(expectedUser), argument.capture(), eq(RECORD_ID), eq("Updated record 10.23728/b2share.b56cd875765a403599859177fced08ae"));
        verifyNoInteractions(publicationService);

        // check created document
        ElterDocument updatedDocument = argument.getValue();
        assertEquals(
                "TERENO Wüstebach meteorological data",
                updatedDocument.getTitle()
                );
        assertEquals(
                "10 minute interval temperature and precipitation in °C and mm from different sensors of a meteorological station.Sensor names are temperature at 2m, Precipitation_Cum10min_OttNRTtotal, Precipitation_Cum10min_OttRTNRT, Precipitation_Cum10min_OttNRT, Precipitation_Cum10min_Ecotech, Precipitation_Cum10min_RainCap",
                updatedDocument.getDescription()
                );
        assertEquals(
                "10.23728/b2share.b56cd875765a403599859177fced08ae",
                updatedDocument.getImportId()
                );
        assertEquals(
                "signpost",
                updatedDocument.getType()
                );
        assertEquals(
                "Level 0",
                updatedDocument.getDataLevel()
                );
    }

    @Test
    @SneakyThrows
    public void skipInvalidRecord() {
        // setup
        b2shareImportService = new B2shareImportService(
                documentRepository,
                publicationService,
                solrClient,
                invalidB2shareResponseUrl
                );

        // given
        given(solrClient.query(any(String.class), any(SolrParams.class), eq(POST)))
            .willReturn(queryResponse);
        given(queryResponse.getResults())
            .willReturn(new SolrDocumentList());

        // when
        b2shareImportService.runImport();

        // then
        verifyNoInteractions(documentRepository);
        verifyNoInteractions(publicationService);
    }
}
