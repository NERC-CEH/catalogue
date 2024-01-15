package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
    public void successfullyGetServiceAgreements() {
        //Given
        val solrQuery = new SolrQuery();
        solrQuery.setQuery(QUERY);

        ServiceAgreement serviceAgreement1 = new ServiceAgreement();
        ServiceAgreement serviceAgreement2 = new ServiceAgreement();

        ReflectionTestUtils.setField(serviceAgreement1, "title", TITLE_1);
        ReflectionTestUtils.setField(serviceAgreement2, "title", TITLE_2);

        val serviceAgreementSolrIndex1 = new ServiceAgreementSolrIndex(new ServiceAgreementModel(serviceAgreement1));
        val serviceAgreementSolrIndex2 = new ServiceAgreementSolrIndex(new ServiceAgreementModel(serviceAgreement2));

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
    public void failToGetServiceAgreements() {
        //Given
        when(solrClient.query(eq(SERVICE_AGREEMENT), any(SolrParams.class), eq(POST)))
            .thenThrow(new ServiceAgreementException("Test"));

        //When
        Assertions.assertThrows(ServiceAgreementException.class, () ->
                solrServiceAgreementSearch.query(QUERY)
        );
    }
}
