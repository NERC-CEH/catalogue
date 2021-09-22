package uk.ac.ceh.gateway.catalogue.serviceagreement;

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
class SolrServiceAgreementSearchTest {

    public static final String TITLE_1 = "title1";
    public static final String TITLE_2 = "title2";
    public static final String QUERY = "queryTest";
    private static final String SERVICE_AGREEMENT = "service-agreement";

    @Mock
    private SolrClient solrClient;

    @InjectMocks
    private SolrServiceAgreementSearch solrServiceAgreementSearch;

    @Test
    @SneakyThrows
    public void successfullyGetDeimsSites() {
        //Given
        val solrQuery = new SolrQuery();
        solrQuery.setQuery(QUERY);

//        val deimsSolrIndex1 = new DeimsSolrIndex(new DeimsSite(SITE_1, "https://example.com/", "1"));
//        val deimsSolrIndex2 = new DeimsSolrIndex(new DeimsSite(SITE_2, "https://example.com/", "2"));

        val serviceAgreementSolrIndex1 = new ServiceAgreementSolrIndex(new ServiceAgreement()); //need to make constructor for service agreement and add a title
        val serviceAgreementSolrIndex2 = new ServiceAgreementSolrIndex(new ServiceAgreement()); //possibly use serviceagreement model instead

        val response = mock(QueryResponse.class);

        given(solrClient.query(eq(SERVICE_AGREEMENT), any(SolrParams.class), eq(POST)))
                .willReturn(response);
        given(response.getBeans(ServiceAgreementSolrIndex.class))
                .willReturn(Arrays.asList(
                        serviceAgreementSolrIndex1,
                        serviceAgreementSolrIndex2
                ));

        //When
        List<ServiceAgreementSolrIndex> result = solrServiceAgreementSearch.query(QUERY);

        //Then
        assertThat(result.get(0).getTitle(), equalTo(TITLE_1));
        assertThat(result.get(1).getTitle(), equalTo(TITLE_2));
    }


    @Test
    @SneakyThrows
    public void ThrowSolrServerException() {
        //Given
        when(solrClient.query(eq(SERVICE_AGREEMENT), any(SolrParams.class), eq(POST))).thenThrow(new SolrServerException("Test"));

        //When
        Assertions.assertThrows(SolrServerException.class, () ->
                solrServiceAgreementSearch.query(QUERY)
        );
    }
}