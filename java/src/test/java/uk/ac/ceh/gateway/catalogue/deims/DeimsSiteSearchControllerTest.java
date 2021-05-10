package uk.ac.ceh.gateway.catalogue.deims;

import lombok.SneakyThrows;
import org.apache.solr.client.solrj.SolrServerException;
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
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

class DeimsSiteSearchControllerTest {

    public static final String SITE_1 = "site1";
    public static final String SITE_2 = "site2";
    public static final String QUERY = "queryTest";
    private static final String DEIMS = "deims";

    @Mock
    private DeimsSolrQueryService DEIMSService;

    @InjectMocks
    private DeimsSiteSearchController controller;

    @BeforeEach
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
        controller = new DeimsSiteSearchController(DEIMSService);
    }

    @Test
    public void getSitesTest() throws SolrServerException {
        //Given
        List<DeimsSolrIndex> expected = new ArrayList<>();
        DeimsSite deimsSite1 = new DeimsSite();
        deimsSite1.setTitle(SITE_1);
        deimsSite1.setId(new DeimsSite.Id());
        DeimsSite deimsSite2 = new DeimsSite();
        deimsSite2.setTitle(SITE_2);
        deimsSite2.setId(new DeimsSite.Id());
        DeimsSolrIndex deimsSolrIndex1 = new DeimsSolrIndex(deimsSite1);
        DeimsSolrIndex deimsSolrIndex2 = new DeimsSolrIndex(deimsSite2);
        expected.add(deimsSolrIndex1);
        expected.add(deimsSolrIndex2);

        when(DEIMSService.query(QUERY)).thenReturn(expected);

        //When
        List<DeimsSolrIndex> result = controller.getSites(QUERY);

        //Then
        assertThat(result.get(0).getTitle(), equalTo(SITE_1));
        assertThat(result.get(1).getTitle(), equalTo(SITE_2));
    }


    @Test
    @SneakyThrows
    public void ThrowSolrServerException() {
        //Given
        when(DEIMSService.query(QUERY)).thenThrow(new SolrServerException("Test"));

        //When
        Assertions.assertThrows(SolrServerException.class, () -> {
            controller.getSites(QUERY);
        });
    }
}