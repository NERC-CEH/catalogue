package uk.ac.ceh.gateway.catalogue.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import uk.ac.ceh.gateway.catalogue.datalabs.DatalabsDocument;

@WithMockCatalogueUser
@ActiveProfiles("test")
@DisplayName("DatalabsDocumentController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(
    controllers=DatalabsDocumentController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
class DatalabsDocumentControllerTest {
    @MockBean private DocumentRepository documentRepository;

    private DatalabsDocumentController controller;

    @BeforeEach
    void setup() {
        controller = new DatalabsDocumentController(documentRepository);
    }

    @Test
    public void checkCanCreateDatalabsDocument() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser();
        DatalabsDocument document = new DatalabsDocument();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String message = "new DataLabs document";
        String catalogue = "catalogue";

        given(documentRepository.saveNew(user, document, catalogue, message)).willReturn(document);

        //When
        ResponseEntity<MetadataDocument> actual = controller.newDatalabsDocument(user, document, catalogue);

        //Then
        verify(documentRepository).saveNew(user, document, catalogue, message);
        assertThat("Should have 201 CREATED status", actual.getStatusCode(), equalTo(HttpStatus.CREATED));
    }

    @Test
    public void checkCanEditDatalabsDocument() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser();
        DatalabsDocument document = new DatalabsDocument();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String fileId = "test";
        String message = "Edited document: test";

        given(documentRepository.read(fileId)).willReturn(new DatalabsDocument().setMetadata(MetadataInfo.builder().build()));
        given(documentRepository.save(user, document, fileId, message)).willReturn(document);

        //When
        ResponseEntity<MetadataDocument> actual = controller.saveDatalabsDocument(user, fileId, document);

        //Then
        verify(documentRepository).save(user, document, fileId, "Edited document: test");
        verify(documentRepository).read(fileId);
        assertThat("Should have 200 OK status", actual.getStatusCode(), equalTo(HttpStatus.OK));
    }
}
