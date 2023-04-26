package uk.ac.ceh.gateway.catalogue.elter;

import lombok.SneakyThrows;
import lombok.val;

import org.apache.commons.io.IOUtils;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import uk.ac.ceh.gateway.catalogue.elter.ElterDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.publication.PublicationService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

import static org.hamcrest.CoreMatchers.equalTo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
@DisplayName("SITESImportService")
public class SITESImportServiceTest {
    private SITESImportService sitesImportService;
    private MockRestServiceServer mockServer;
    private String testSitemapUrl;
    private byte[] datasetHtml;

    private static final String SOLR_COLLECTION = "documents";
    private static final String CATALOGUE = "elter";
    private static final String RECORD_ID = "00000000-0000-0000-0000-000000000000";

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private PublicationService publicationService;

    @Mock
    private SolrClient solrClient;

    @Test
    @SneakyThrows
    public void importNewRecord() {
        // setup
        val restTemplate = new RestTemplate();
        val queryResponse = mock(QueryResponse.class);
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        datasetHtml = IOUtils.toByteArray(getClass().getResource("sites-dataset.html"));
        testSitemapUrl = getClass().getResource("sites-sitemap-with-dataset.xml").toString();

        CatalogueUser expectedUser = new CatalogueUser()
            .setUsername("SITES metadata import")
            .setEmail("info@fieldsites.se");

        sitesImportService = new SITESImportService(
                documentRepository,
                publicationService,
                restTemplate,
                solrClient,
                testSitemapUrl
                );

        // given
        given(solrClient.query(eq(SOLR_COLLECTION), any(SolrParams.class), eq(POST)))
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

        mockServer
            .expect(requestTo(equalTo("https://meta.fieldsites.se/objects/P8rtv97XQIOXtgQEiEjwokOt")))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(datasetHtml, MediaType.TEXT_HTML));

        // when
        sitesImportService.runImport();

        // then
        mockServer.verify();
        ArgumentCaptor<ElterDocument> argument = ArgumentCaptor.forClass(ElterDocument.class);
        verify(documentRepository).saveNew(eq(expectedUser), argument.capture(), eq(CATALOGUE), eq("Create new record https://hdl.handle.net/11676.1/P8rtv97XQIOXtgQEiEjwokOt"));
        verify(publicationService).transition(expectedUser, RECORD_ID, "ykhm7b");
        verify(publicationService).transition(expectedUser, RECORD_ID, "re4vkb");

        // NOTE: the html resource has been sanitised by converting
        // "ö" and "–" characters to "o" and "-" respectively.
        // assertions would fail otherwise due to garbled characters.
        // 
        // these characters seem to be correctly handled in real use
        // so we just sidestep them for testing.
        ElterDocument createdDocument = argument.getValue();
        assertEquals(
                "Starling reproduction from Grimso, Centroid of Research Area, 1981-2022",
                createdDocument.getTitle()
                );
        assertEquals(
                "Inventory on starling (Sturnus vulgaris) reproduction. Nest boxes are surveyed annually, distributed in six sub areas (with 25 nest boxes each) within the research area. Boxes are checked for laid eggs and hatched fledglings.\nGrimso Wildlife Research Station (2023). Starling reproduction from Grimso, Centroid of Research Area, 1981-2022 [Data set]. Swedish Infrastructure for Ecosystem Science (SITES). https://hdl.handle.net/11676.1/P8rtv97XQIOXtgQEiEjwokOt",
                createdDocument.getDescription()
                );
        assertEquals(
                "https://hdl.handle.net/11676.1/P8rtv97XQIOXtgQEiEjwokOt",
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
}
