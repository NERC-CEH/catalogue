package uk.ac.ceh.gateway.catalogue.deims;

import lombok.SneakyThrows;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

class DEIMSSiteSearchControllerTest {

    public static final String SITE_1 = "site1";
    public static final String SITE_2 = "site2";
    public static final String QUERY = "queryTest";
    private static final String DEIMS = "deims";

    @Mock
    private DEIMSSolrQueryService DEIMSService;

    @InjectMocks
    private DEIMSSiteSearchController controller;

    @BeforeEach
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
        controller = new DEIMSSiteSearchController(DEIMSService);
    }

    @Test
    public void getSitesTest() throws DocumentIndexingException, SolrServerException {
        //Given
        List<DeimsSite> expected = new ArrayList<>();
        DeimsSite deimsSite1 = new DeimsSite();
        deimsSite1.setTitle("site1");
        DeimsSite deimsSite2 = new DeimsSite();
        deimsSite2.setTitle("site2");
        expected.add(deimsSite1);
        expected.add(deimsSite2);

        when(DEIMSService.query(QUERY)).thenReturn(expected);

        //When
        List<DeimsSite> result = controller.getSites(QUERY);

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