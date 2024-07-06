package uk.ac.ceh.gateway.catalogue.indexing.mapserver;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.MapDataDefinition;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MapServerIndexingServiceTest {

    @TempDir Path directory;
    @Mock BundledReaderService<MetadataDocument> reader;
    @Mock DocumentListingService listingService;
    @Mock DataRepository<CatalogueUser> repo;
    @Mock IndexGenerator<MetadataDocument, MapFile> indexGenerator;
    @Mock MetadataDocument metadataDocument;
    @Mock MapFile mapFile;

    private MapServerIndexingService service;

    @BeforeEach
    public void init() {
        service = new MapServerIndexingService(reader, listingService, repo, indexGenerator, directory.toFile());
    }

    @Test
    void checkThatServiceGeminiDocumentWithServiceDefinitionIsIndexed() {
        //Given
        val mapDataDefinition = new MapDataDefinition();
        mapDataDefinition.setData(List.of(new MapDataDefinition.DataSource()));
        val document = new GeminiDocument();
        document.setType("service");
        document.setMapDataDefinition(mapDataDefinition);

        //When
        val canIndex = service.canIndex(document);

        //Then
        assertTrue(canIndex);
    }

    @Test
    void checkThatServiceGeminiDocumentWithoutDataIsNotIndexed() {
        //Given
        val mapDataDefinition = new MapDataDefinition();
        val document = new GeminiDocument();
        document.setType("service");
        document.setMapDataDefinition(mapDataDefinition);

        //When
        val canIndex = service.canIndex(document);

        //Then
        assertFalse(canIndex);
    }

    @Test
    void checkThatServiceGeminiDocumentEmptyServiceDefinitionIsNotIndexed() {
        //Given
        val mapDataDefinition = new MapDataDefinition();
        mapDataDefinition.setData(Collections.emptyList());
        val document = new GeminiDocument();
        document.setType("service");
        document.setMapDataDefinition(mapDataDefinition);

        //When
        boolean canIndex = service.canIndex(document);

        //Then
        assertFalse(canIndex);
    }

    @Test
    void checkThatDatasetGeminiDocumentIsNotIndexed() {
        //Given
        val document = new GeminiDocument();
        document.setType("dataset");

        //When
        val canIndex = service.canIndex(document);

        //Then
        assertFalse(canIndex);
    }

    @Test
    public void checkThatCanClearOutDirectory() throws Exception {
        //Given
        Path someFile = directory.resolve("SomeFile_default.map");
        Files.createFile(someFile);

        //When
        service.clearIndex();

        //Then
        assertTrue(service.isIndexEmpty());
    }

    @Test
    @SneakyThrows
    void serviceAgreementNotIndexed() {
        //given
        val revId = "Latest";
        val documents = List.of("serviceAgreement1");
        val serviceAgreement = new ServiceAgreement();
        given(reader.readBundle("serviceAgreement1", revId))
            .willReturn(serviceAgreement);

        //when
        service.indexDocuments(documents, revId);

        //then
        verify(indexGenerator, never()).generateIndex(any(GeminiDocument.class));
    }

    @Test
    public void checkThatHavingMapFileMeansIndexIsNotEmpty() throws Exception {
        //Given
        Path someFile = directory.resolve("SomeFile_default.map");
        Files.createFile(someFile);

        //When
        val indexed = service.getIndexedFiles();

        //Then
        assertFalse(service.isIndexEmpty());
        assertThat(indexed, hasItem("SomeFile"));
    }

    @Test
    public void checkThatMultipleProjectionSystemsForFileOnlyReturnOneIndexItem() throws IOException {
        //Given
        Path someFilePath = directory.resolve("SomeFile_default.map");
        Path someFile27700Path = directory.resolve("SomeFile_27700.map");
        Path someFile3857Path = directory.resolve("SomeFile_3857.map");

        Files.createFile(someFilePath);
        Files.createFile(someFile27700Path);
        Files.createFile(someFile3857Path);

        //When
        List<String> indexed = service.getIndexedFiles();

        //Then
        assertThat(indexed.size(), is(1));
        assertThat(indexed, hasItem("SomeFile"));
    }

    @Test
    public void checkThatCanUnIndexAFile() throws Exception {
        //Given
        Path someFilePath = directory.resolve("SomeFile_default.map");
        Path someFile27700Path = directory.resolve("SomeFile_27700.map");

        Files.createFile(someFilePath);
        Files.createFile(someFile27700Path);

        //When
        service.unindexDocuments(Arrays.asList("SomeFile"));

        //Then
        assertTrue(service.isIndexEmpty());
    }

    @Test
    public void checkThatCanIndexAMapFile() throws Exception {
        //Given
        when(metadataDocument.getId()).thenReturn("document-id");
        when(mapFile.getDocument()).thenReturn(metadataDocument);

        //When
        service.index(mapFile); // Test works when process.exec() is a String not an array of Strings

        //Then
        assertFalse(service.isIndexEmpty());
        assertTrue(new File(directory.toFile(), "document-id_default.map").exists());
    }

    @Test
    public void checkThatDoesntDetectOtherFiles(@TempDir Path tempDir) throws Exception {
        //Given
        Path notARealMapfile = directory.resolve("Not A real mapfile");
        Files.createFile(notARealMapfile);

        //When
        service.getIndexedFiles();

        //Then
        assertTrue(service.isIndexEmpty());
    }

    @Test
    public void checkThatOnlyFindMapFilesWithCorrectExtension() throws Exception {
        //Given
        Path notARealMapfile = directory.resolve("SomeFile_default.map.somethingSlseAtEnd");
        Files.createFile(notARealMapfile);

        //When
        service.getIndexedFiles();

        //Then
        assertTrue(service.isIndexEmpty());
    }

    @Test
    public void checkThatCleansIndexWhenReindexing() throws Exception {
        //Given
        Path lurkingOldFile = directory.resolve("lurking_old_file_default.map");
        Files.createFile(lurkingOldFile);

        //When
        service.indexDocuments(Arrays.asList("lurking_old_file"), "latest");

        //Then
        assertTrue(service.isIndexEmpty());
    }
}
