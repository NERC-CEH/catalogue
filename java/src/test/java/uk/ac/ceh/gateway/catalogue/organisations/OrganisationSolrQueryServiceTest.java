package uk.ac.ceh.gateway.catalogue.organisations;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganisationSolrQueryServiceTest {

    public static final String QUERY = "queryTest";
    private static final String COLLECTION = "organisations";

    @Mock
    private SolrClient solrClient;

    @InjectMocks
    private OrganisationSolrQueryService service;

    @Test
    @SneakyThrows
    public void successfullyGetOrganisations() {
        //Given
        val org1 = new Organisation("id1", "name1", List.of("acronym1", "acronym2"), List.of("aliase1", "aliase2"));
        val org2 = new Organisation("id2", "name2", Collections.emptyList(), Collections.emptyList());
        val orgList = List.of(org1, org2);

        val response = mock(QueryResponse.class);
        given(solrClient.query(eq(COLLECTION), any(SolrQuery.class), eq(POST)))
            .willReturn(response);
        given(response.getBeans(Organisation.class))
            .willReturn(orgList);

        //When
        List<Organisation> result = service.query(QUERY);

        //Then
        assertEquals(result, orgList);
    }


    @Test
    @SneakyThrows
    public void throwSolrServerException() {
        //Given
        when(solrClient.query(eq(COLLECTION), any(SolrQuery.class), eq(POST))).thenThrow(new SolrServerException("Test"));

        //When
        SolrServerException exception = assertThrows(SolrServerException.class, () ->
            service.query(QUERY)
        );

        //Then
        assertThat(exception.getMessage(), containsString("Test"));
    }
}
