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
import uk.ac.ceh.gateway.catalogue.elter.ElterDocument;
import uk.ac.ceh.gateway.catalogue.elter.LinkedDocumentRetrievalService;
import uk.ac.ceh.gateway.catalogue.elter.LinkedElterDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@WithMockCatalogueUser
@ActiveProfiles("test")
@DisplayName("ElterDocumentController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(
    controllers=ElterDocumentController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
class ElterDocumentControllerTest {
    @MockBean private DocumentRepository documentRepository;
    @MockBean private LinkedDocumentRetrievalService linkedDocumentRetrievalService;

    private ElterDocumentController controller;

    @BeforeEach
    void setup() {
        controller = new ElterDocumentController(documentRepository,linkedDocumentRetrievalService);
    }

    @Test
    public void checkCanCreateElterDocument() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        ElterDocument document = new ElterDocument();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String message = "new Elter Document";
        String catalogue = "catalogue";

        given(documentRepository.saveNew(user, document, catalogue, message)).willReturn(document);

        //When
        ResponseEntity<MetadataDocument> actual = controller.newElterDocument(user, document, catalogue);

        //Then
        verify(documentRepository).saveNew(user, document, catalogue, message);
        assertThat("Should have 201 CREATED status", actual.getStatusCode(), equalTo(HttpStatus.CREATED));
    }

    @Test
    public void checkCanEditElterDocument() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        ElterDocument document = new ElterDocument();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String fileId = "test";
        String message = "Edited document: test";

        given(documentRepository.read(fileId)).willReturn(new ElterDocument().setMetadata(MetadataInfo.builder().build()));
        given(documentRepository.save(user, document, fileId, message)).willReturn(document);

        //When
        ResponseEntity<MetadataDocument> actual = controller.updateElterDocument(user, fileId, document);

        //Then
        verify(documentRepository).save(user, document, fileId, "Edited document: test");
        verify(documentRepository).read(fileId);
        assertThat("Should have 200 OK status", actual.getStatusCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void checkCanCreateLinkedElterDocument() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        ElterDocument document = new ElterDocument();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String message = "new linked Elter Document";
        String catalogue = "catalogue";
        LinkedElterDocument linkedDocument = new LinkedElterDocument("https://catalogue.ceh.ac.uk/documents/some-id-to-fetch?format=json","foo type");

        given(documentRepository.saveNew(user, document, catalogue, message)).willReturn(document);
        given(linkedDocumentRetrievalService.get("https://catalogue.ceh.ac.uk/documents/some-id-to-fetch?format=json")).willReturn(document);

        //When
        ResponseEntity<MetadataDocument> actual = controller.newLinkedElterDocument(user, linkedDocument, catalogue);

        //Then
        verify(documentRepository).saveNew(user, document, catalogue, message);
        assertThat("Should have 201 CREATED status", actual.getStatusCode(), equalTo(HttpStatus.CREATED));
    }
}
