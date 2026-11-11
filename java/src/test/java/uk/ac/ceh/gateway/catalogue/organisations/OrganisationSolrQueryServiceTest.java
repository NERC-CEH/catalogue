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
        val org1 = Organisation.builder().id("id1").name("name1").acronyms(List.of("acronym1", "acronym2")).aliases(List.of("aliase1", "aliase2"))
            .city("York").region("England").country("United Kingdom").countryCode("GB").types(List.of("education", "funder")).website("https://www.york.ac.uk").build();
        val org2 = Organisation.builder().id("id2").name("name2").acronyms(Collections.emptyList()).aliases(Collections.emptyList()).build();
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
    public void getsAnOrganisationByItsIdentifierAsItIs() {
        //Given
        val response = mock(QueryResponse.class);
        val york = Organisation.builder().id("https://ror.org/04m01e293").name("University of York").build();
        given(solrClient.query(eq(COLLECTION), any(SolrQuery.class), eq(POST))).willReturn(response);
        given(response.getBeans(Organisation.class)).willReturn(List.of(york));

        //When
        List<Organisation> result = service.get("https://ror.org/04m01e293");

        //Then
        assertEquals(List.of(york), result);
        val query = org.mockito.ArgumentCaptor.forClass(SolrQuery.class);
        org.mockito.Mockito.verify(solrClient).query(eq(COLLECTION), query.capture(), eq(POST));
        assertThat(query.getValue().getFilterQueries()[0], org.hamcrest.Matchers.is("{!term f=id}https://ror.org/04m01e293"));
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
