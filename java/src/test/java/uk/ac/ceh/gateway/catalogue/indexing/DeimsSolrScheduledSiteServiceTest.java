package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static uk.ac.ceh.gateway.catalogue.indexing.DeimsSolrScheduledSiteService.*;

@ExtendWith(MockitoExtension.class)
public class DeimsSolrScheduledSiteServiceTest {

    private DeimsSolrScheduledSiteService target;

    private MockRestServiceServer mockServer;

    private static final String ADDRESS = "https://example.com";

    private static final String COLLECTION = "deims";

    @Mock
    private SolrClient solrClient;

    @BeforeEach
    public void init() {
        val restTemplate = new RestTemplate();
        target = new DeimsSolrScheduledSiteService(restTemplate, solrClient, ADDRESS);
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }


    @Test
    @SneakyThrows
    public void successfullyGetDIEMSSites() {
        //Given
        val response = IOUtils.toString(getClass().getResource("deimssites.json"), StandardCharsets.UTF_8);
        mockServer.expect(requestTo(ADDRESS))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        //When
        target.fetchDEIMSSitesAndAddToSolr();

        //Then
        mockServer.verify();
        verify(solrClient).deleteByQuery(COLLECTION, "*:*");
        verify(solrClient, times(3)).addBean(eq("deims"), any(DeimsSolrIndex.class));
        verify(solrClient).commit();
    }

    @Test
    @SneakyThrows
    public void ThrowDocumentIndexingException() {

        //Given
        val response = IOUtils.toString(getClass().getResource("deimssites.json"), StandardCharsets.UTF_8);
        mockServer.expect(requestTo(ADDRESS))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        when(solrClient.addBean(eq("deims"), any(DeimsSolrIndex.class))).thenThrow(new SolrServerException("Test"));

        //When
        Assertions.assertThrows(DocumentIndexingException.class, () -> {
            target.fetchDEIMSSitesAndAddToSolr();
        });
    }
}