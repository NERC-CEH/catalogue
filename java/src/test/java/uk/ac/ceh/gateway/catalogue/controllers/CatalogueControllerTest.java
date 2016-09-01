package uk.ac.ceh.gateway.catalogue.controllers;

import org.junit.Test;
import org.junit.Before;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.CatalogueResource;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

public class CatalogueControllerTest {
    private @Mock DocumentRepository documentRepository;
    private CatalogueController controller;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        controller = new CatalogueController(
            documentRepository
        );
    }

    @Test
    public void getCurrentCatalogue() throws Exception {
        //Given
        String file = "123-456-789";
        MetadataDocument document = new GeminiDocument()
            .setId(file);
        document.attachMetadata(
            new MetadataInfo()
                .setCatalogue("eidc")
        );
        given(documentRepository.read(file)).willReturn(document);
        
        //When
        controller.currentCatalogue(CatalogueUser.PUBLIC_USER, file);
        
        //Then
        verify(documentRepository).read(file);
    }
    
    @Test
    public void updateCatalogue() throws Exception {
        //Given
        String file = "123-456-789";
        CatalogueResource catalogueResource = new CatalogueResource("1", "eidc");
        
        MetadataDocument document = new GeminiDocument()
            .setId(file);
        document.attachMetadata(
            new MetadataInfo()
                .setCatalogue("eidc")
        );
        given(documentRepository.read(file)).willReturn(document);
        given(documentRepository.save(
            CatalogueUser.PUBLIC_USER,
            document,
            file,
            "Catalogues of 123-456-789 changed."
        )).willReturn(document);
        
        //When
        controller.updateCatalogue(CatalogueUser.PUBLIC_USER, file, catalogueResource);
        
        //Then
        verify(documentRepository).read(file);
        verify(documentRepository).save(CatalogueUser.PUBLIC_USER, document, file, "Catalogues of 123-456-789 changed.");
    }
    
}
