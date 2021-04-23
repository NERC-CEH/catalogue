package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class DEIMSSolrScheduledSiteServiceTest {

    private DEIMSSolrScheduledSiteService target;

    private MockRestServiceServer mockServer;

    private static final String ADDRESS = "https://example.com";

    @Mock
    private SolrClient solrClient;

    @Mock IndexGenerator<GeminiDocument, SolrIndex> indexGenerator;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
        val restTemplate = new RestTemplate();
        target = new DEIMSSolrScheduledSiteService(restTemplate, solrClient, ADDRESS);
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }


    @Test
    @SneakyThrows
    public void successfullyGetDIEMSSites() {
        //Given
        val response = IOUtils.toString(getClass().getResource("deimssites.json"), "UTF-8");
        mockServer.expect(requestTo(ADDRESS))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        //When
        target.fetchDEIMSSitesAndAddToSolr();

        //Then
        mockServer.verify();
        verify(solrClient).deleteByQuery("*:*");
        verify(solrClient).addBean(any(SolrInputDocument.class));
        verify(solrClient).commit();
    }

    @Test
    @SneakyThrows
    public void ThrowDocumentIndexingException() {
        Assertions.assertThrows(DocumentIndexingException.class, () -> {
            //Given
            val response = IOUtils.toString(getClass().getResource("deimssites.json"), "UTF-8");
            mockServer.expect(requestTo(ADDRESS))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

            when(solrClient.addBean(any(Object.class))).thenThrow(new SolrServerException("Test"));

            //When
            target.fetchDEIMSSitesAndAddToSolr();
        });
    }
}