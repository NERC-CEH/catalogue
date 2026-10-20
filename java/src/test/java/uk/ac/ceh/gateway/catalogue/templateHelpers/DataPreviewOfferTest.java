package uk.ac.ceh.gateway.catalogue.templateHelpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.config.DownloadUrlProperties;
import uk.ac.ceh.gateway.catalogue.datapreviewer.DataPreviewProperties;
import uk.ac.ceh.gateway.catalogue.datapreviewer.OpenDataAccess;
import uk.ac.ceh.gateway.catalogue.gemini.DistributionInfo;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadService;

import java.time.Duration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DataPreviewOfferTest {
    private static final String ID = "abc";
    private static final String SUPPORTING_DOCS = "https://data-package.ceh.ac.uk/sd/abc.zip";

    private DocumentRepository repository;
    private OpenDataAccess openDataAccess;
    private UploadService uploadService;
    private GeminiDocument document;

    @BeforeEach
    void init() throws Exception {
        repository = mock(DocumentRepository.class);
        openDataAccess = mock(OpenDataAccess.class);
        uploadService = mock(UploadService.class);
        document = mock(GeminiDocument.class);
        when(repository.read(ID)).thenReturn(document);
        when(openDataAccess.isOpen(document)).thenReturn(true);
        when(document.getDistributionFormats()).thenReturn(List.of());
        when(document.getOnlineResources()).thenReturn(List.of());
    }

    private DataPreviewOffer offer(boolean enabled) {
        var properties = mock(DownloadUrlProperties.class);
        when(properties.getRegexSupportingDocs()).thenReturn("https://data-package\\.ceh\\.ac\\.uk/sd/.*");
        when(properties.getRegexOrderManDownload()).thenReturn("http(s?)://catalogue\\.ceh\\.ac\\.uk/download\\?fileIdentifier=.*");
        when(properties.getRegexOrder()).thenReturn("https://order-eidc\\.ceh\\.ac\\.uk/resources/.{8}/order\\?*.*");
        var dataPreview = new DataPreviewProperties(enabled, null, null, 10, 50, 262144, 20971520L, 200000,
            Duration.ofSeconds(20));
        return new DataPreviewOffer(dataPreview, repository, openDataAccess,
            new DownloadOrderDetailsService(properties));
    }

    private void csv() {
        when(document.getDistributionFormats()).thenReturn(
            List.of(DistributionInfo.builder().name("Comma-separated values (csv)").build()));
    }

    @Test
    void offeredForOpenCsvRecord() {
        csv();

        assertThat(offer(true).offered(ID), is(true));
    }

    @Test
    void offeredForOpenRecordWithOnlySupportingDocs() {
        when(document.getOnlineResources()).thenReturn(List.of(
            OnlineResource.builder().url(SUPPORTING_DOCS).function("information").build()));

        assertThat(offer(true).offered(ID), is(true));
    }

    @Test
    void notOfferedWhenDisabled() {
        csv();

        assertThat(offer(false).offered(ID), is(false));
    }

    @Test
    void notOfferedForRegistrationRequired() {
        csv();
        when(openDataAccess.isOpen(document)).thenReturn(false);

        assertThat(offer(true).offered(ID), is(false));
    }

    @Test
    void notOfferedWithoutCsvOrDocs() {
        when(document.getDistributionFormats()).thenReturn(
            List.of(DistributionInfo.builder().name("GeoTIFF").build()));

        assertThat(offer(true).offered(ID), is(false));
    }

    @Test
    void notOfferedForNonGeminiRecord() throws Exception {
        when(repository.read(ID)).thenReturn(mock(MetadataDocument.class));

        assertThat(offer(true).offered(ID), is(false));
    }

    @Test
    void notOfferedWhenRecordReadFails() throws Exception {
        when(repository.read(ID)).thenThrow(new DocumentRepositoryException("gone", new RuntimeException()));
        assertThat(offer(true).offered(ID), is(false));

        doThrow(new IllegalStateException("boom")).when(repository).read(ID);
        assertThat(offer(true).offered(ID), is(false));
    }

    @Test
    void neverCallsHubbub() {
        csv();

        offer(true).offered(ID);

        verifyNoInteractions(uploadService);
    }
}
