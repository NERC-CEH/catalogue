package uk.ac.ceh.gateway.catalogue.model;

import static org.hamcrest.Matchers.contains;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;


public class CatalogueResourceTest {
    
    
    @Before
    public void setUp() {
    }

    @Test
    public void metadataInfoCataloguesUpdated() {
        //Given
        MetadataInfo original = new MetadataInfo();
        GeminiDocument document = new GeminiDocument()
            .setId("f24a3b8b-69b7-43e5-a2f0-67011b45baba")
            .setTitle("Test");
        document.attachMetadata(original);
        CatalogueResource catalogueResource = CatalogueResource
            .builder()
            .id("f24a3b8b-69b7-43e5-a2f0-67011b45baba")
            .title("Test")
            .catalogue("EIDC")
            .catalogue("CEH")
            .catalogue("xyz")
            .build();
        
        //When
        catalogueResource.updateCatalogues(original);
        
        //Then
        assertThat(
            "Catalogues added to MetadataInfo",
            original.getCatalogues(),
            contains("EIDC", "CEH", "xyz")
        );
    }
    
}
