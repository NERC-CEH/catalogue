package uk.ac.ceh.gateway.catalogue.repository;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataConflictException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.repository.checks.SaveCheck;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Persistence, and how the repository runs its {@link SaveCheck}s. What each check accepts or
 * refuses is tested with the check itself, in {@code repository/checks}.
 */
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
    @Mock GitRepoWrapper repo;

    private GitDocumentRepository documentRepository;

    /** What the checks saw, in the order they ran. */
    private final List<String> checked = new ArrayList<>();

    @BeforeEach
    public void setup() {
        documentRepository = repositoryWith();
    }

    private GitDocumentRepository repositoryWith(SaveCheck... checks) {
        return new GitDocumentRepository(
            documentTypeLookupService,
            documentReader,
            documentIdentifierService,
            documentWritingService,
            documentBundleReader,
            repo,
            List.of(checks)
        );
    }

    /** A check that records that it ran, and on what. */
    private SaveCheck recording(String name) {
        return (user, incoming, id, stored) -> checked.add(name + " " + id + " " + incoming.getUri());
    }

    /** A check that refuses every save. */
    private static SaveCheck refusing() {
        return (user, incoming, id, stored) -> {
            throw new IllegalStateException("refused");
        };
    }

    /** A check that looks at the stored version. */
    private static SaveCheck readingStored() {
        return (user, incoming, id, stored) -> stored.get();
    }

    private InputStream upload() {
        return new ByteArrayInputStream("{}".getBytes());
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

    @Test
    @SneakyThrows
    @DisplayName("checks run in order on the document as it will be written, then it is committed")
    public void checksRunInOrderBeforeCommitting() {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = (GeminiDocument) new GeminiDocument().setMetadata(MetadataInfo.builder().build());
        given(documentIdentifierService.generateUri("tulips")).willReturn("http://localhost:8080/id/tulips");
        documentRepository = repositoryWith(recording("first"), recording("second"));

        //When
        documentRepository.save(user, document, "tulips", "message");

        //Then the checks saw the id and record URI the document is written with
        assertThat(checked).containsExactly(
            "first tulips http://localhost:8080/id/tulips",
            "second tulips http://localhost:8080/id/tulips"
        );
        verify(repo).save(eq(user), eq("tulips"), eq("message"), any(MetadataInfo.class), any(), isNull(), eq(document));
    }

    @Test
    @SneakyThrows
    @DisplayName("a refused save commits nothing, and later checks do not run")
    public void aRefusedSaveCommitsNothing() {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = (GeminiDocument) new GeminiDocument().setMetadata(MetadataInfo.builder().build());
        given(documentIdentifierService.generateUri("tulips")).willReturn("http://localhost:8080/id/tulips");
        documentRepository = repositoryWith(refusing(), recording("after"));

        //When / Then
        assertThrows(IllegalStateException.class, () -> documentRepository.save(user, document, "tulips", "message"));
        assertThat(checked).isEmpty();
        verifyNoInteractions(repo);
    }

    @Test
    @SneakyThrows
    @DisplayName("a refused upload commits nothing at all, not even the raw upload")
    public void aRefusedUploadCommitsNothingAtAll() {
        // The raw upload is committed before the document; checks that ran only after it (as the
        // uniqueness check once did) left an orphaned raw blob behind when they refused.
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        given(documentReader.read(any(), any(), any())).willReturn(new GeminiDocument());
        given(documentIdentifierService.generateFileId(null)).willReturn("test");
        documentRepository = repositoryWith(refusing());

        //When / Then
        assertThrows(IllegalStateException.class,
            () -> documentRepository.save(user, upload(), MediaType.APPLICATION_JSON, "GEMINI_DOCUMENT", "eidc", "message"));
        verifyNoInteractions(repo);
    }

    @Test
    @SneakyThrows
    @DisplayName("an upload is checked once, before the raw upload is committed")
    public void anUploadIsCheckedOnce() {
        // The upload path once ran some checks before the raw commit and every check again before
        // the document commit, by then against the upload itself.
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        given(documentReader.read(any(), any(), any())).willReturn(new GeminiDocument());
        given(documentIdentifierService.generateFileId(null)).willReturn("test");
        given(documentIdentifierService.generateUri("test")).willReturn("http://localhost:8080/id/test");
        documentRepository = repositoryWith(recording("check"));

        //When
        documentRepository.save(user, upload(), MediaType.APPLICATION_JSON, "GEMINI_DOCUMENT", "eidc", "message");

        //Then
        assertThat(checked).hasSize(1);
        verify(repo).save(eq(user), eq("test"), eq("message"), any(MetadataInfo.class), any());
        verify(repo).save(eq(user), eq("test"), eq("File upload for id: test"), any(MetadataInfo.class), any(), isNull(), any());
    }

    @Test
    @SneakyThrows
    @DisplayName("the stored version is read once, however many checks look at it")
    public void theStoredVersionIsReadOnce() {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = (GeminiDocument) new GeminiDocument().setMetadata(MetadataInfo.builder().build());
        given(documentIdentifierService.generateUri("tulips")).willReturn("http://localhost:8080/id/tulips");
        documentRepository = repositoryWith(readingStored(), readingStored());

        //When
        documentRepository.save(user, document, "tulips", "message");

        //Then
        verify(documentBundleReader, times(1)).readBundle("tulips");
    }
}
