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
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.ukems.UkemsDocument;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@WithMockCatalogueUser
@ActiveProfiles("test")
@DisplayName("UkemsDocumentController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(
    controllers=UkemsDocumentController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
class UkemsDocumentControllerTest {
    @MockBean private DocumentRepository documentRepository;

    private UkemsDocumentController controller;

    @BeforeEach
    void setup() {
        controller = new UkemsDocumentController(documentRepository);
    }

    @Test
    public void checkCanCreateUkemsDocument() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        UkemsDocument document = new UkemsDocument();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String message = "new UK-EMS document";
        String catalogue = "catalogue";

        given(documentRepository.saveNew(user, document, catalogue, message)).willReturn(document);

        //When
        ResponseEntity<MetadataDocument> actual = controller.newUkemsDocument(user, document, catalogue);

        //Then
        verify(documentRepository).saveNew(user, document, catalogue, message);
        assertThat("Should have 201 CREATED status", actual.getStatusCode(), equalTo(HttpStatus.CREATED));
    }

    @Test
    public void checkCanEditUkemsDocument() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        UkemsDocument document = new UkemsDocument();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String fileId = "test";
        String message = "Edited document: test";

        given(documentRepository.read(fileId)).willReturn(new UkemsDocument().setMetadata(MetadataInfo.builder().build()));
        given(documentRepository.save(user, document, fileId, message)).willReturn(document);

        //When
        ResponseEntity<MetadataDocument> actual = controller.saveUkemsDocument(user, fileId, document);

        //Then
        verify(documentRepository).save(user, document, fileId, "Edited document: test");
        verify(documentRepository).read(fileId);
        assertThat("Should have 200 OK status", actual.getStatusCode(), equalTo(HttpStatus.OK));
    }
}
