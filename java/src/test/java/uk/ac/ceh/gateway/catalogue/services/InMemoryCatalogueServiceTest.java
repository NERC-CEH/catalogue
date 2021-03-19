package uk.ac.ceh.gateway.catalogue.services;

import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;


public class InMemoryCatalogueServiceTest {
    private CatalogueService catalogueService;

    @Test
    public void retrieveAllCatalogues() {
        //given
        Catalogue t1 = Catalogue.builder().id("t1").title("t").url("u").build();
        Catalogue t2 = Catalogue.builder().id("t2").title("t").url("u").build();
        Catalogue t3 = Catalogue.builder().id("t3").title("t").url("u").build();
        
        catalogueService = new InMemoryCatalogueService("t1", t1, t2, t3);
        
        //when
        List<Catalogue> actual = catalogueService.retrieveAll();
        
        //then
        assertThat("should equal list of catalogues", actual.contains(t1));
        assertThat("should equal list of catalogues", actual.contains(t2));
        assertThat("should equal list of catalogues", actual.contains(t3));
    }
    
}
