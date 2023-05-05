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
import uk.ac.ceh.gateway.catalogue.imp.Model;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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

    @BeforeEach
    public void setup() {
        documentRepository = new GitDocumentRepository(
                            documentTypeLookupService,
                            documentReader,
                            documentIdentifierService,
                            documentWritingService,
                            documentBundleReader,
                            repo);
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
        CatalogueUser user = new CatalogueUser().setUsername("test").setEmail("test@example.com");
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
        verify(repo).save(eq(user), eq("test"), eq("File upload for id: test"), any(MetadataInfo.class), any());
    }

    @Test
    @SneakyThrows
    public void saveNewGeminiDocument() {
        //Given
        CatalogueUser user = new CatalogueUser().setUsername("test").setEmail("test@example.com");
        GeminiDocument document = new GeminiDocument();
        String message = "new Gemini document";
        String catalogue = "test";

        given(documentIdentifierService.generateFileId()).willReturn("test");
        given(documentIdentifierService.generateUri("test")).willReturn("http://localhost:8080/id/test");

        //When
        documentRepository.saveNew(user, document, catalogue, message);

        //Then
        verify(repo).save(eq(user), eq("test"), eq("new Gemini document"), any(MetadataInfo.class), any());
    }

    @Test
    @SneakyThrows
    public void saveEditedGeminiDocument() {
        //Given
        String id = "tulips";
        CatalogueUser user = new CatalogueUser().setUsername("test").setEmail("test@example.com");
        MetadataInfo metadataInfo = MetadataInfo.builder().build();
        MetadataDocument incomingDocument = new GeminiDocument()
            .setMetadata(metadataInfo);
        String message = "message";

        given(documentIdentifierService.generateUri(id)).willReturn("http://localhost:8080/id/test");

        //When
        documentRepository.save(user, incomingDocument, "tulips", message);

        //Then
        verify(repo).save(eq(user), eq(id), eq(message), any(MetadataInfo.class), any());
    }

    @Test
    @SneakyThrows
    public void checkCanDeleteAFile() {
        //Given
        CatalogueUser user = new CatalogueUser().setUsername("test").setEmail("test@example.com");

        //When
        documentRepository.delete(user, "id");

        //Then
        verify(repo).delete(user, "id");
    }

    @Test
    @SneakyThrows
    public void checkMetadataInfoUpdated() {
        //Given
        CatalogueUser editor = new CatalogueUser()
            .setUsername("editor")
            .setEmail("editor@example.com");
        String file = "3c25e9b7-d3dd-41be-ae29-e8979bb462a2";
        String message = "Test message";
        MetadataInfo metadataInfo = MetadataInfo.builder()
            .catalogue("eidc")
            .documentType("MODEL_DOCUMENT")
            .rawType("application/json")
            .state("published")
            .build();
        metadataInfo.addPermission(Permission.EDIT, "editor");
        MetadataDocument document = new Model()
            .setId(file)
            .setMetadata(metadataInfo);

        given(documentIdentifierService.generateUri(file)).willReturn("https://catalogue.ceh.ac.uk/id/3c25e9b7-d3dd-41be-ae29-e8979bb462a2");

        //When
        documentRepository.save(editor, document, file, message);

        //Then
        verify(repo).save(eq(editor), eq(file), eq(message), eq(metadataInfo), any());

    }

}