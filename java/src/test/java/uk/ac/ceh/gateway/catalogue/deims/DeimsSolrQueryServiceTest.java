package uk.ac.ceh.gateway.catalogue.deims;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeimsSolrQueryServiceTest {

    public static final String SITE_1 = "site1";
    public static final String SITE_2 = "site2";
    public static final String QUERY = "queryTest";
    private static final String DEIMS = "deims";

    @Mock
    private SolrClient solrClient;

    @InjectMocks
    private DeimsSolrQueryService service;

    @Test
    @SneakyThrows
    public void successfullyGetDeimsSites() {
        //Given
        val solrQuery = new SolrQuery();
        solrQuery.setQuery(QUERY);

        val deimsSolrIndex1 = new DeimsSolrIndex(new DeimsSite(SITE_1, "https://example.com/", "1"));
        val deimsSolrIndex2 = new DeimsSolrIndex(new DeimsSite(SITE_2, "https://example.com/", "2"));

        val response = mock(QueryResponse.class);

        given(solrClient.query(eq(DEIMS), any(SolrParams.class), eq(POST)))
            .willReturn(response);
        given(response.getBeans(DeimsSolrIndex.class))
            .willReturn(Arrays.asList(
                deimsSolrIndex1,
                deimsSolrIndex2
            ));

        //When
        List<DeimsSolrIndex> result = service.query(QUERY);

        //Then
        assertThat(result.get(0).getTitle(), equalTo(SITE_1));
        assertThat(result.get(1).getTitle(), equalTo(SITE_2));
    }


    @Test
    @SneakyThrows
    public void ThrowSolrServerException() {
        //Given
        when(solrClient.query(eq(DEIMS), any(SolrParams.class), eq(POST))).thenThrow(new SolrServerException("Test"));

        //When
        Assertions.assertThrows(SolrServerException.class, () ->
            service.query(QUERY)
        );
    }
}
