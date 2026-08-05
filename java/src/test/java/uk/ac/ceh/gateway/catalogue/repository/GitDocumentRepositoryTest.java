package uk.ac.ceh.gateway.catalogue.repository;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.document.writing.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataConflictException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.ResourceIdentifierExistsException;
import uk.ac.ceh.gateway.catalogue.services.ResourceIdentifierLookupService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class GitDocumentRepositoryTest {
    @Mock
    DocumentIdentifierService documentIdentifierService;
    @Mock
    DocumentReadingService documentReader;
    @Mock
    BundledReaderService<MetadataDocument> documentBundleReader;
    @Mock
    DocumentWritingService documentWritingService;
    @Mock
    DocumentTypeLookupService documentTypeLookupService;
    @Mock
    ResourceIdentifierLookupService resourceIdentifierLookupService;
    @Mock GitRepoWrapper repo;

    private GitDocumentRepository documentRepository;

    @BeforeEach
    public void setup() {
        documentRepository = new GitDocumentRepository(
                            documentTypeLookupService,
                            documentReader,
                            documentIdentifierService,
                            documentWritingService,
                            documentBundleReader,
                            resourceIdentifierLookupService,
                            repo);
        lenient().when(resourceIdentifierLookupService.findDocumentIdsByRi(any())).thenReturn(List.of());
    }

    @Test
    @SneakyThrows
    public void readLatestDocument() {
        //When
        documentRepository.read("file");

        //Then
        verify(documentBundleReader).readBundle("file");
    }

    @Test
    @SneakyThrows
    public void readDocumentAtRevision() {
        //When
        documentRepository.read("file", "special");

        //Then
        verify(documentBundleReader).readBundle("file", "special");
    }

    @Test
    @SneakyThrows
    public void savingMultipartFileStoresInputStreamIntoRepo() {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        InputStream inputStream = new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root></root>".getBytes());
        String documentType = "GEMINI_DOCUMENT";
        String message = "message";
        GeminiDocument document = new GeminiDocument();
        String catalogue = "ceh";

        given(documentReader.read(any(), any(), any())).willReturn(document);
        given(documentIdentifierService.generateFileId(null)).willReturn("test");
        given(documentIdentifierService.generateUri("test")).willReturn("http://localhost:8080/id/test");

        //When
        documentRepository.save(user, inputStream, MediaType.TEXT_XML, documentType, catalogue, message);

        //Then
        verify(repo).save(eq(user), eq("test"), eq(message), any(MetadataInfo.class), any());
        verify(repo).save(eq(user), eq("test"), eq("File upload for id: test"), any(MetadataInfo.class), any(), isNull(), any());
    }

    @Test
    @SneakyThrows
    public void saveNewGeminiDocument() {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = new GeminiDocument();
        String message = "new Gemini document";
        String catalogue = "test";

        given(documentIdentifierService.generateFileId()).willReturn("test");
        given(documentIdentifierService.generateUri("test")).willReturn("http://localhost:8080/id/test");

        //When
        documentRepository.saveNew(user, document, catalogue, message);

        //Then
        verify(repo).save(eq(user), eq("test"), eq("new Gemini document"), any(MetadataInfo.class), any(), isNull(), any());
    }

    @Test
    @SneakyThrows
    public void saveEditedGeminiDocument() {
        //Given
        String id = "tulips";
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        MetadataInfo metadataInfo = MetadataInfo.builder().build();
        MetadataDocument incomingDocument = new GeminiDocument()
            .setMetadata(metadataInfo);
        String message = "message";

        given(documentIdentifierService.generateUri(id)).willReturn("http://localhost:8080/id/test");

        //When
        documentRepository.save(user, incomingDocument, "tulips", message);

        //Then
        verify(repo).save(eq(user), eq(id), eq(message), any(MetadataInfo.class), any(), isNull(), any());
    }

    @Test
    @SneakyThrows
    public void checkCanDeleteAFile() {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");

        //When
        documentRepository.delete(user, "id");

        //Then
        verify(repo).delete(user, "id");
    }

    @Test
    @SneakyThrows
    public void checkCanDeleteAFileWithAnExplicitMessage() {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");

        //When
        documentRepository.delete(user, "id", "admin delete document: id (reason: orphaned)");

        //Then
        verify(repo).delete(user, "id", "admin delete document: id (reason: orphaned)");
    }

    /**
     * {@code GitDocumentRepository.delete(user, id, message)} must wrap a {@code DataRepositoryException}
     * into the checked {@code DocumentRepositoryException} its interface declares, exactly as every other
     * method here does. This is the one place that translation was never directly exercised: the admin
     * delete route's own tests mock {@code DocumentRepository} at the interface level, so this concrete
     * class's exception handling was previously dark.
     */
    @Test
    public void deleteWithAMessageWrapsARepositoryFailure() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        doThrow(new uk.ac.ceh.components.datastore.DataRepositoryException("disk full"))
            .when(repo).delete(user, "id", "a message");

        //When / Then
        assertThrows(DocumentRepositoryException.class,
            () -> documentRepository.delete(user, "id", "a message"));
    }

    @Test
    @SneakyThrows
    public void duplicateResourceIdentifierThrowsConflict() {
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        MetadataInfo metadataInfo = MetadataInfo.builder().build();

        // Realistic RI: codespace + code
        ResourceIdentifier ri = ResourceIdentifier.builder()
            .codeSpace("ukceh.eidc")
            .code("fafa99")
            .build();

        GeminiDocument document = (GeminiDocument) new GeminiDocument()
            .setMetadata(metadataInfo)
            .setResourceIdentifiers(List.of(ri));

        String currentId = "tulips";
        given(documentIdentifierService.generateUri(currentId))
            .willReturn("http://localhost:8080/id/" + currentId);

        given(resourceIdentifierLookupService.findDocumentIdsByRi("ukceh.eidc:fafa99"))
            .willReturn(List.of("existing-doc"));

        assertThrows(
            ResourceIdentifierExistsException.class,
            () -> documentRepository.save(user, document, currentId, "message")
        );
    }

    @Test
    @SneakyThrows
    public void resavingOwnResourceIdentifierDoesNotThrow() {
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        MetadataInfo metadataInfo = MetadataInfo.builder().build();

        ResourceIdentifier ri = ResourceIdentifier.builder()
            .codeSpace("ukceh.eidc")
            .code("fafa99")
            .build();

        GeminiDocument document = (GeminiDocument) new GeminiDocument()
            .setMetadata(metadataInfo)
            .setResourceIdentifiers(List.of(ri));

        String currentId = "tulips";
        given(documentIdentifierService.generateUri(currentId))
            .willReturn("http://localhost:8080/id/" + currentId);

        // The only owner of the identifier is the record being saved.
        given(resourceIdentifierLookupService.findDocumentIdsByRi("ukceh.eidc:fafa99"))
            .willReturn(List.of(currentId));

        // Should not throw: re-saving a record that owns its own identifier is allowed.
        documentRepository.save(user, document, currentId, "message");
    }

    @Test
    public void saveWithExpectedRevisionPropagatesConflict() throws Exception {
        //Given the wrapper rejects the save as a conflict
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        MetadataDocument document = new GeminiDocument();
        document.setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        doThrow(new MetadataConflictException("stale", document))
            .when(repo).save(any(), eq("doc1"), any(), any(), any(), eq("rev1"), any());

        //When/Then saving with that stale revision surfaces the conflict
        assertThrows(MetadataConflictException.class, () ->
            documentRepository.save(user, document, "doc1", "Edited document: doc1", "rev1"));
    }
}
