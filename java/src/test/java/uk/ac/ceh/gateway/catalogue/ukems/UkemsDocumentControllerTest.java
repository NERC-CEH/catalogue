package uk.ac.ceh.gateway.catalogue.ukems;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@WithMockCatalogueUser
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("UkemsDocumentController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class UkemsDocumentControllerTest {
    @MockitoBean private DocumentRepository documentRepository;

    private CachedDataRepository cachedDataRepository;
    private UkemsDocumentController controller;

    @BeforeEach
    void setup() {
        cachedDataRepository = mock(CachedDataRepository.class);
        controller = new UkemsDocumentController(documentRepository, cachedDataRepository);
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
        given(documentRepository.save(user, document, fileId, message, "rev1")).willReturn(document);

        //When
        ResponseEntity<MetadataDocument> actual = controller.saveUkemsDocument(user, fileId, document, "rev1");

        //Then
        verify(documentRepository).save(user, document, fileId, "Edited document: test", "rev1");
        verify(documentRepository).read(fileId);
        assertThat("Should have 200 OK status", actual.getStatusCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void putEmitsETagOfNewRevisionAfterSave() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        UkemsDocument document = new UkemsDocument();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String fileId = "test";
        String message = "Edited document: test";

        given(documentRepository.read(fileId)).willReturn(new UkemsDocument().setMetadata(MetadataInfo.builder().build()));
        given(documentRepository.save(user, document, fileId, message, "rev1")).willReturn(document);
        given(cachedDataRepository.getDocumentRevisionToken(fileId)).willReturn("rev2");

        //When
        ResponseEntity<MetadataDocument> actual = controller.saveUkemsDocument(user, fileId, document, "rev1");

        //Then the response carries the NEW per-document revision as its ETag (quoted per HTTP)
        assertThat(actual.getHeaders().getETag(), is("\"rev2\""));
    }
}
