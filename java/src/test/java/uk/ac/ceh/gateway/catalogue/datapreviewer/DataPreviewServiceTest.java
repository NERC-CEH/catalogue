package uk.ac.ceh.gateway.catalogue.datapreviewer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.ac.ceh.components.datastore.git.GitFileNotFoundException;
import uk.ac.ceh.gateway.catalogue.gemini.AccessLimitation;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.HubbubResponse;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.HubbubResponse.FileInfo;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadService;

class DataPreviewServiceTest {

    private static final String N07 = "http://vocab.nerc.ac.uk/collection/N07/current/";
    private static final String ID = "rec-1";
    private static final String DATA = "eidchub";
    private static final String DOCS = "supporting-documents";

    @TempDir
    Path tmp;

    private UploadService uploadService;
    private DocumentRepository documentRepository;
    private DocumentConverter documentConverter;
    private PreviewContentReader contentReader;
    private DataPreviewService service;

    @BeforeEach
    void setUp() {
        uploadService = mock(UploadService.class);
        documentRepository = mock(DocumentRepository.class);
        DataPreviewProperties properties = new DataPreviewProperties(
            true, tmp, tmp, 10, 50, 262144, 20971520, 200000, Duration.ofSeconds(20));
        CsvSampler sampler = new CsvSampler();
        documentConverter = new DocumentConverter(sampler, properties);
        contentReader = spy(new PreviewContentReader(new PreviewFileResolver(), sampler, documentConverter, properties));
        service = new DataPreviewService(
            documentRepository, new OpenDataAccess(), new PreviewListing(uploadService, properties), contentReader);
        when(uploadService.get(eq(ID), eq(DOCS), anyInt(), anyInt())).thenReturn(response(List.of(), 1, 0));
    }

    @AfterEach
    void shutDownConverter() {
        documentConverter.shutdown();
    }

    private void record(String accessCode) throws DocumentRepositoryException {
        GeminiDocument document = mock(GeminiDocument.class);
        MetadataInfo metadata = mock(MetadataInfo.class);
        Multimap<Permission, String> permissions = ArrayListMultimap.create();
        permissions.put(Permission.VIEW, "public");
        when(document.getMetadata()).thenReturn(metadata);
        when(metadata.getPermissions()).thenReturn(permissions);
        when(document.getAvailability()).thenReturn("Available");
        when(document.getAccessLimitation())
            .thenReturn(new AccessLimitation("noLimitations", "", "", N07 + accessCode + "/"));
        when(documentRepository.read(ID)).thenReturn(document);
    }

    private static FileInfo file(String path, String mimeType, String status) {
        return new FileInfo(123L, ID, DATA, null, null, null, null, null, path, status, "sha-" + path, mimeType);
    }

    private static FileInfo csv(String path) {
        return file(path, "text/csv", "VALID");
    }

    private static HubbubResponse response(List<FileInfo> files, int lastPage, int totalFiles) {
        return new HubbubResponse(files, null, new HubbubResponse.Meta(1, lastPage, files.size(), totalFiles));
    }

    private void data(List<FileInfo> files, int lastPage, int totalFiles) {
        when(uploadService.get(eq(ID), eq(DATA), anyInt(), anyInt())).thenReturn(response(files, lastPage, totalFiles));
    }

    @Test
    void manifestListsCsvsInPathOrderUpToTen() throws Exception {
        record("UNRS");
        List<FileInfo> files = new ArrayList<>();
        for (char c = 'b'; c < 'b' + 13; c++) {
            files.add(csv(c + ".csv"));
        }
        files.add(csv("a.csv"));
        files.add(file("map.shp", "application/octet-stream", "VALID"));
        files.add(file("roads.shp", "application/octet-stream", "VALID"));
        data(files, 1, 16);

        PreviewManifest manifest = service.manifest(ID);

        assertThat(manifest.csvFiles(), hasSize(10));
        assertThat(manifest.csvFiles().get(0).path(), is("a.csv"));
        assertThat(manifest.csvFiles().get(9).path(), is("j.csv"));
        assertThat(manifest.csvFiles().get(0).label(), is("a.csv"));
        assertThat(manifest.csvFiles().get(0).bytes(), is(123L));
        assertThat(manifest.csvFiles().get(0).sha256(), is("sha-a.csv"));
        assertThat(manifest.csvCount(), is(14));
        assertThat(manifest.csvCountIsMinimum(), is(false));
        assertThat(manifest.totalFiles(), is(16));
        verify(uploadService).get(ID, DATA, 1, 10000);
    }

    @Test
    void manifestCountsCsvByExtensionWhenMimeMissing() throws Exception {
        record("UNRS");
        data(List.of(file("X.CSV", null, "VALID")), 1, 1);

        PreviewManifest manifest = service.manifest(ID);

        assertThat(manifest.csvCount(), is(1));
        assertThat(manifest.csvFiles().get(0).path(), is("X.CSV"));
    }

    @Test
    void manifestTreatsNullBytesAsZero() throws Exception {
        record("UNRS");
        data(List.of(new FileInfo(null, ID, DATA, null, null, null, null, null, "a.csv", "VALID", "s", "text/csv")), 1, 1);

        assertThat(service.manifest(ID).csvFiles().get(0).bytes(), is(0L));
    }

    @Test
    void manifestSkipsUnreliableStatuses() throws Exception {
        record("UNRS");
        data(List.of(
            file("missing.csv", "text/csv", "MISSING"),
            file("invalid.csv", "text/csv", "INVALID"),
            file("writing.csv", "text/csv", "WRITING"),
            file("from.csv", "text/csv", "MOVING_FROM"),
            file("to.csv", "text/csv", "MOVING_TO"),
            csv("good.csv")), 1, 6);

        PreviewManifest manifest = service.manifest(ID);

        assertThat(manifest.csvFiles().stream().map(PreviewManifest.PreviewFile::path).toList(), contains("good.csv"));
        assertThat(manifest.csvCount(), is(1));
    }

    @Test
    void manifestFlagsMinimumWhenListingExceedsOnePage() throws Exception {
        record("UNRS");
        data(List.of(csv("a.csv")), 3, 25000);

        PreviewManifest manifest = service.manifest(ID);

        assertThat(manifest.csvCountIsMinimum(), is(true));
        assertThat(manifest.totalFiles(), is(25000));
        verify(uploadService).get(ID, DATA, 1, 10000);
        verify(uploadService, never()).get(eq(ID), eq(DATA), eq(2), anyInt());
        verify(uploadService, never()).get(eq(ID), eq(DATA), eq(3), anyInt());
    }

    @Test
    void manifestFallsBackToListedCountWithoutMeta() throws Exception {
        record("UNRS");
        when(uploadService.get(eq(ID), eq(DATA), anyInt(), anyInt()))
            .thenReturn(new HubbubResponse(List.of(csv("a.csv"), csv("b.csv")), null, null));

        PreviewManifest manifest = service.manifest(ID);

        assertThat(manifest.totalFiles(), is(2));
        assertThat(manifest.csvCountIsMinimum(), is(false));
    }

    @Test
    void manifestIsEmptyWhenHubbubReturns404() throws Exception {
        record("UNRS");
        HttpClientErrorException notFound = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);
        when(uploadService.get(anyString(), anyString(), anyInt(), anyInt())).thenThrow(notFound);

        assertThat(service.manifest(ID), is(PreviewManifest.empty()));
    }

    @Test
    void manifestPropagatesOtherHubbubFailures() throws Exception {
        record("UNRS");
        HttpClientErrorException forbidden = HttpClientErrorException.create(HttpStatus.FORBIDDEN, "Forbidden", null, null, null);
        when(uploadService.get(eq(ID), eq(DATA), anyInt(), anyInt())).thenThrow(forbidden);

        assertThrows(HttpClientErrorException.Forbidden.class, () -> service.manifest(ID));
    }

    @Test
    void manifestListsSupportingDocuments() throws Exception {
        record("UNRS");
        data(List.of(), 1, 0);
        when(uploadService.get(eq(ID), eq(DOCS), anyInt(), anyInt())).thenReturn(response(List.of(
            file("guide.pdf", "application/pdf", "VALID"),
            file("dictionary.rtf", "application/rtf", "VALID"),
            file("old.rtf", "application/rtf", "MISSING")), 1, 3));

        PreviewManifest manifest = service.manifest(ID);

        assertThat(manifest.documents().stream().map(PreviewManifest.PreviewFile::path).toList(),
            contains("dictionary.rtf", "guide.pdf"));
        assertThat(manifest.csvFiles(), is(empty()));
        verify(uploadService).get(ID, DOCS, 1, 1000);
    }

    @Test
    void labelsUseShortestUnambiguousPath() {
        assertThat(PreviewListing.labels(List.of("a/df.csv", "b/df.csv", "c/x.csv")),
            contains("a/df.csv", "b/df.csv", "x.csv"));
    }

    @Test
    void labelsLengthenOnlyAsFarAsNeeded() {
        assertThat(PreviewListing.labels(List.of("x/a/df.csv", "y/a/df.csv", "z/b/df.csv", "top.csv")),
            contains("x/a/df.csv", "y/a/df.csv", "b/df.csv", "top.csv"));
    }

    @Test
    void csvRefusesPathNotInManifest() throws Exception {
        record("UNRS");
        data(List.of(csv("a.csv")), 1, 1);
        Files.createDirectories(tmp.resolve(ID));
        Files.writeString(tmp.resolve(ID).resolve("secret.csv"), "h\n1\n");

        assertThat(service.csv(ID, "secret.csv").isPresent(), is(false));
        verify(contentReader, never()).csv(anyString(), anyString(), any());
    }

    @Test
    void csvReadsListedFile() throws Exception {
        record("UNRS");
        data(List.of(csv("dir/a.csv")), 1, 1);
        Files.createDirectories(tmp.resolve(ID).resolve("dir"));
        Files.writeString(tmp.resolve(ID).resolve("dir/a.csv"), "name,value\nx,1\ny,2\n");

        CsvSample sample = service.csv(ID, "dir/a.csv").orElseThrow();

        assertThat(sample.previewable(), is(true));
        assertThat(sample.header(), contains("name", "value"));
        assertThat(sample.rows(), contains(List.of("x", "1"), List.of("y", "2")));
        verify(contentReader).csv(ID, "dir/a.csv", "sha-dir/a.csv");
    }

    @Test
    void csvListedButAbsentFromShareIsNotPreviewable() throws Exception {
        record("UNRS");
        data(List.of(csv("gone.csv")), 1, 1);

        assertThat(service.csv(ID, "gone.csv").orElseThrow(), is(CsvSample.notPreviewable()));
    }

    @Test
    void documentReadsListedFile() throws Exception {
        record("UNRS");
        data(List.of(), 1, 0);
        when(uploadService.get(eq(ID), eq(DOCS), anyInt(), anyInt()))
            .thenReturn(response(List.of(file("notes.txt", "text/plain", "VALID")), 1, 1));
        Files.createDirectories(tmp.resolve(ID));
        Files.writeString(tmp.resolve(ID).resolve("notes.txt"), "Notes on the survey");

        DocumentPreview preview = service.document(ID, "notes.txt").orElseThrow();

        assertThat(preview.kind(), is(DocumentPreview.Kind.TEXT));
        assertThat(preview.text(), containsString("Notes on the survey"));
        verify(contentReader).document(ID, "notes.txt", "sha-notes.txt");
    }

    @Test
    void documentRefusesPathNotInManifest() throws Exception {
        record("UNRS");
        data(List.of(csv("a.csv")), 1, 1);

        assertThat(service.document(ID, "a.csv").isPresent(), is(false));
        verify(contentReader, never()).document(anyString(), anyString(), any());
    }

    @Test
    void documentListedButAbsentFromShareIsNotPreviewable() throws Exception {
        record("UNRS");
        data(List.of(), 1, 0);
        when(uploadService.get(eq(ID), eq(DOCS), anyInt(), anyInt()))
            .thenReturn(response(List.of(file("gone.pdf", "application/pdf", "VALID")), 1, 1));

        assertThat(service.document(ID, "gone.pdf").orElseThrow().kind(), is(DocumentPreview.Kind.NOT_PREVIEWABLE));
    }

    @Test
    void documentBusyPropagates() throws Exception {
        record("UNRS");
        data(List.of(), 1, 0);
        when(uploadService.get(eq(ID), eq(DOCS), anyInt(), anyInt()))
            .thenReturn(response(List.of(file("notes.txt", "text/plain", "VALID")), 1, 1));
        DocumentConverter busy = mock(DocumentConverter.class);
        when(busy.convert(any())).thenThrow(new PreviewBusyException());
        DataPreviewProperties properties = new DataPreviewProperties(
            true, tmp, tmp, 10, 50, 262144, 20971520, 200000, Duration.ofSeconds(20));
        Files.createDirectories(tmp.resolve(ID));
        Files.writeString(tmp.resolve(ID).resolve("notes.txt"), "Notes");
        DataPreviewService busyService = new DataPreviewService(documentRepository, new OpenDataAccess(),
            new PreviewListing(uploadService, properties),
            new PreviewContentReader(new PreviewFileResolver(), new CsvSampler(), busy, properties));

        assertThrows(PreviewBusyException.class, () -> busyService.document(ID, "notes.txt"));
    }

    @Test
    void refusesClosedRecord() throws Exception {
        record("RACC");

        assertThrows(PreviewNotOpenException.class, () -> service.manifest(ID));
        assertThrows(PreviewNotOpenException.class, () -> service.csv(ID, "a.csv"));
        assertThrows(PreviewNotOpenException.class, () -> service.document(ID, "a.pdf"));
        verify(uploadService, never()).get(anyString(), eq(DATA), anyInt(), anyInt());
        verifyNoInteractions(contentReader);
    }

    @Test
    void refusesRecordThatIsNotGemini() throws Exception {
        when(documentRepository.read(ID)).thenReturn(mock(uk.ac.ceh.gateway.catalogue.model.LinkDocument.class));

        assertThrows(PreviewNotOpenException.class, () -> service.manifest(ID));
    }

    @Test
    void missingRecordIsNoSuchElement() throws Exception {
        when(documentRepository.read(ID)).thenThrow(
            new DocumentRepositoryException("Cannot read file: " + ID, new GitFileNotFoundException("absent")));

        assertThrows(NoSuchElementException.class, () -> service.manifest(ID));
        assertThrows(NoSuchElementException.class, () -> service.csv(ID, "a.csv"));
        assertThrows(NoSuchElementException.class, () -> service.document(ID, "a.pdf"));
    }

    @Test
    void nullRecordIsNoSuchElement() throws Exception {
        when(documentRepository.read(ID)).thenReturn(null);

        assertThrows(NoSuchElementException.class, () -> service.manifest(ID));
    }

    @Test
    void otherRepositoryFailurePropagates() throws Exception {
        DocumentRepositoryException failure =
            new DocumentRepositoryException("Cannot read file: " + ID, new IOException("share down"));
        when(documentRepository.read(ID)).thenThrow(failure);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> service.manifest(ID));
        assertThat(thrown.getCause(), is(sameInstance(failure)));
    }
}
