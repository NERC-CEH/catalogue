package uk.ac.ceh.gateway.catalogue.controllers;

import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.Before;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.CatalogueException;
import uk.ac.ceh.gateway.catalogue.model.CatalogueResource;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;

public class CatalogueControllerTest {
    private @Mock DocumentRepository documentRepository;
    private @Mock CatalogueService catalogueService;
    private CatalogueController controller;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        controller = new CatalogueController(
            documentRepository,
            catalogueService
        );
    }

    @Test
    public void getAllCatalogues() throws Exception {
        //given
        Catalogue a = Catalogue.builder().id("a").title("a").url("a").build();
        Catalogue b = Catalogue.builder().id("b").title("b").url("b").build();
        Catalogue c = Catalogue.builder().id("c").title("c").url("c").build();

        given(catalogueService.retrieveAll()).willReturn(Arrays.asList(a, b, c));

        //when
        List<Catalogue> actual = controller.catalogues(null, null).getBody();

        //then
        verify(catalogueService).retrieveAll();
        assertThat("should be list of catalogues", actual, contains(a, b, c));
    }

    @Test
    public void getCataloguesMinusB() throws Exception {
        //given
        Catalogue a = Catalogue.builder().id("a").title("a").url("a").build();
        Catalogue b = Catalogue.builder().id("b").title("b").url("b").build();
        Catalogue c = Catalogue.builder().id("c").title("c").url("c").build();

        given(catalogueService.retrieveAll()).willReturn(Arrays.asList(a, b, c));
        given(catalogueService.retrieve("b")).willReturn(b);

        //when
        List<Catalogue> actual = controller.catalogues("b", null).getBody();

        //then
        verify(catalogueService).retrieveAll();
        verify(catalogueService).retrieve("b");
        assertThat("should be list of catalogues without B", actual, contains(a, c));
    }
    
    @Test
    public void getCataloguesWithUnknownCatalogue() throws Exception {
        //given
        Catalogue a = Catalogue.builder().id("a").title("a").url("a").build();
        Catalogue b = Catalogue.builder().id("b").title("b").url("b").build();
        Catalogue c = Catalogue.builder().id("c").title("c").url("c").build();

        given(catalogueService.retrieveAll()).willReturn(Arrays.asList(a, b, c));
        given(catalogueService.retrieve("x")).willThrow(CatalogueException.class);

        //when
        List<Catalogue> actual = controller.catalogues("x", null).getBody();

        //then
        verify(catalogueService).retrieveAll();
        verify(catalogueService).retrieve("x");
        assertThat("should be list of all catalogues", actual, contains(a, b, c));
    }
    
    @Test
    public void getCataloguesForIdentifier() throws Exception {
        //given
        Catalogue a = Catalogue.builder().id("a").title("a").url("a").build();
        Catalogue b = Catalogue.builder().id("b").title("b").url("b").build();
        Catalogue c = Catalogue.builder().id("c").title("c").url("c").build();
        
        MetadataDocument document = new GeminiDocument()
            .setMetadata(MetadataInfo.builder().catalogue("a").build());

        given(catalogueService.retrieveAll()).willReturn(Arrays.asList(a, b, c));
        given(documentRepository.read("identifier")).willReturn(document);
        given(catalogueService.retrieve("a")).willReturn(a);

        //when
        List<Catalogue> actual = controller.catalogues(null, "identifier").getBody();

        //then
        verify(catalogueService).retrieveAll();
        verify(documentRepository).read("identifier");
        verify(catalogueService).retrieve("a");
        assertThat("should be list of catalogues without identifier's catalogue (A)", actual, contains(b, c));
    }
    
    @Test
    public void getCataloguesForUnknownIdentifier() throws Exception {
        //given
        Catalogue a = Catalogue.builder().id("a").title("a").url("a").build();
        Catalogue b = Catalogue.builder().id("b").title("b").url("b").build();
        Catalogue c = Catalogue.builder().id("c").title("c").url("c").build();

        given(catalogueService.retrieveAll()).willReturn(Arrays.asList(a, b, c));
        given(documentRepository.read("unknown")).willThrow(DocumentRepositoryException.class);

        //when
        List<Catalogue> actual = controller.catalogues(null, "unknown").getBody();

        //then
        verify(catalogueService).retrieveAll();
        verify(documentRepository).read("unknown");
        assertThat("should be list of all catalogues", actual, contains(a, b, c));
    }

    @Test
    public void getCurrentCatalogue() throws Exception {
        //Given
        String file = "123-456-789";
        MetadataDocument document = new GeminiDocument()
            .setId(file)
            .setMetadata(
                MetadataInfo.builder()
                .catalogue("eidc")
                .build()
        );
        given(documentRepository.read(file)).willReturn(document);

        //When
        controller.currentCatalogue(CatalogueUser.PUBLIC_USER, file);

        //Then
        verify(documentRepository).read(file);
    }

    @Test(expected = DocumentRepositoryException.class)
    public void getUnknownFile() throws Exception {
        //Given
        String file = "123-456-789";
        given(documentRepository.read(file)).willThrow(
            new DocumentRepositoryException("Test", new Exception())
        );

        //When
        controller.currentCatalogue(CatalogueUser.PUBLIC_USER, file);

        //Then
        fail("Expected DocumentRepositoryException");
    }

    @Test
    public void updateCatalogue() throws Exception {
        //Given
        String file = "123-456-789";
        CatalogueResource catalogueResource = new CatalogueResource("1", "eidc");

        MetadataDocument document = new GeminiDocument()
            .setId(file)
            .setMetadata(
                MetadataInfo.builder()
                .catalogue("eidc")
                .build()
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
