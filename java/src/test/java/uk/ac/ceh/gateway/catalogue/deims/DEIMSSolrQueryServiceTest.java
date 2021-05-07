package uk.ac.ceh.gateway.catalogue.deims;

import lombok.SneakyThrows;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DEIMSSolrQueryServiceTest {

    public static final String SITE_1 = "site1";
    public static final String SITE_2 = "site2";
    public static final String QUERY = "queryTest";
    private static final String DEIMS = "deims";

    @Mock
    private SolrClient solrClient;

    @Mock
    private QueryResponse queryResponse;

    @InjectMocks
    private DEIMSSolrQueryService service;

    @BeforeEach
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
        service = new DEIMSSolrQueryService(solrClient);
    }

    @Test
    @SneakyThrows
    public void successfullyGetDIEMSSites() {
        //Given
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set(DEIMS, QUERY);

        List<DeimsSite> expected = new ArrayList<>();
        DeimsSite deimsSite1 = new DeimsSite();
        deimsSite1.setTitle("site1");
        DeimsSite deimsSite2 = new DeimsSite();
        deimsSite2.setTitle("site2");
        expected.add(deimsSite1);
        expected.add(deimsSite2);

        QueryResponse response = mock(QueryResponse.class);
        when(solrClient.query(any(SolrQuery.class))).thenReturn(response);
        when(solrClient.query(solrQuery).getBeans(DeimsSite.class)).thenReturn(expected);

        //When
        List<DeimsSite> result = service.query(QUERY);

        //Then
        SolrQuery query = new SolrQuery();
        query.set(DEIMS, QUERY);
        assertThat(result.get(0).getTitle(), equalTo(SITE_1));
        assertThat(result.get(1).getTitle(), equalTo(SITE_2));
    }


    @Test
    @SneakyThrows
    public void ThrowSolrServerException() {
        //Given
        when(solrClient.query(any(SolrQuery.class))).thenThrow(new SolrServerException("Test"));

        //When
        Assertions.assertThrows(SolrServerException.class, () -> {
            service.query(QUERY);
        });
    }
}