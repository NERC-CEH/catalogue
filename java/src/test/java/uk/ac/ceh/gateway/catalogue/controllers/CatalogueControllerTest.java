package uk.ac.ceh.gateway.catalogue.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CatalogueControllerTest {
    private @Mock DocumentRepository documentRepository;
    private @Mock CatalogueService catalogueService;
    @InjectMocks private CatalogueController controller;

    @Test
    public void getAllCatalogues() {
        //given
        Catalogue a = Catalogue.builder().id("a").title("a").url("a").build();
        Catalogue b = Catalogue.builder().id("b").title("b").url("b").build();
        Catalogue c = Catalogue.builder().id("c").title("c").url("c").build();

        given(catalogueService.retrieveAll()).willReturn(Arrays.asList(a, b, c));

        //when
        List<Catalogue> actual = controller.catalogues(null, null).getBody();

        //then
        verify(catalogueService).retrieveAll();
        assertThat("should be list of catalogues", actual.contains(a));
        assertThat("should be list of catalogues", actual.contains(b));
        assertThat("should be list of catalogues", actual.contains(c));
    }

    @Test
    public void getCataloguesMinusB() {
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
        assertThat("should be list of catalogues", actual.contains(a));
        assertThat("should be list of catalogues", actual.contains(c));
    }

    @Test
    public void getCataloguesWithUnknownCatalogue() {
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
        assertThat("should be list of catalogues", actual.contains(a));
        assertThat("should be list of catalogues", actual.contains(b));
        assertThat("should be list of catalogues", actual.contains(c));
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
        assertThat("should be list of catalogues", actual.contains(b));
        assertThat("should be list of catalogues", actual.contains(c));
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
        assertThat("should be list of catalogues", actual.contains(a));
        assertThat("should be list of catalogues", actual.contains(b));
        assertThat("should be list of catalogues", actual.contains(c));
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

    @Test
    public void getUnknownFile() {
        Assertions.assertThrows(DocumentRepositoryException.class, () -> {
            //Given
            String file = "123-456-789";
            given(documentRepository.read(file)).willThrow(
                    new DocumentRepositoryException("Test", new Exception())
            );

            //When
            controller.currentCatalogue(CatalogueUser.PUBLIC_USER, file);

            //Then
            fail("Expected DocumentRepositoryException");
        });
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
