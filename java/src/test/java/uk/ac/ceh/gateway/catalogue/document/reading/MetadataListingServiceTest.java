package uk.ac.ceh.gateway.catalogue.document.reading;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;
import uk.ac.ceh.gateway.catalogue.document.UnknownContentTypeException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MetadataListingServiceTest {
    @Mock private DataRepository<CatalogueUser> repo;
    @Mock private DocumentListingService listingService;
    @Mock private BundledReaderService<MetadataDocument> documentBundleReader;
    private MetadataListingService service;
    private final List<String> defaultResourceTypes = Arrays.asList("Dataset","Series","Service");
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    @SneakyThrows
    public void initMocks() {
        service = new MetadataListingService(repo,
                                            listingService,
                                            documentBundleReader);

        logAppender = new ListAppender<>();
        logAppender.start();
        ((Logger) LoggerFactory.getLogger(MetadataListingService.class)).addAppender(logAppender);
    }

    @AfterEach
    public void teardown() {
        ((Logger) LoggerFactory.getLogger(MetadataListingService.class)).detachAppender(logAppender);
    }

    @Test
    @SneakyThrows
    public void onlyPublicDocumentsForCatalogue() {
        //given
        DataRevision<CatalogueUser> revision = new DataRevision<CatalogueUser>() {
            @Override
            public String getRevisionID() {
                return "current";
            }

            @Override
            public String getMessage() {
                return null;
            }

            @Override
            public String getShortMessage() {
                return null;
            }

            @Override
            public CatalogueUser getAuthor() {
                return null;
            }
        };
        Multimap<Permission, String> publicPermissions = ImmutableListMultimap.of(Permission.VIEW, "public");
        Multimap<Permission, String> draftPermissions = ImmutableListMultimap.of(Permission.VIEW, "another");
        MetadataInfo publicMeta = MetadataInfo.builder().catalogue("eidc").permissions(publicPermissions).state("published").build();
        MetadataInfo draftMeta = MetadataInfo.builder().catalogue("eidc").permissions(draftPermissions).state("draft").build();
        MetadataDocument public1 = new GeminiDocument().setId("123").setMetadata(publicMeta);
        MetadataDocument public2 = new GeminiDocument().setId("456").setMetadata(publicMeta);
        MetadataDocument draft = new GeminiDocument().setId("789").setMetadata(draftMeta);
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList("123", "456", "789"));
        when(documentBundleReader.readBundle(any(String.class), any(String.class))).thenReturn(public1, public2, draft);
        when(repo.getLatestRevision()).thenReturn(revision);

        //when
        List<String> actual = service.getPublicDocumentsOfCatalogue("eidc");

        //then
        assertThat(actual.contains("123"), is(true));
        assertThat(actual.contains("456"), is(true));
    }


    @Test
    public void checkThatReadsDocumentsListFromDataRepositiory() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        //Given
        String revision = "revision";
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList("uid"));

        Multimap<Permission, String> permissions = HashMultimap.create();
        permissions.put(Permission.VIEW, "public");
        MetadataInfo metadata = MetadataInfo.builder().permissions(permissions).catalogue("eidc").state("published").build();
        GeminiDocument document = new GeminiDocument();
        document.setId("uid");
        document.setResourceType(Keyword.builder().value("Dataset").build());
        document.setMetadata(metadata);

        when(documentBundleReader.readBundle("uid", revision)).thenReturn(document);

        //When
        List<String> ids = service.getPublicDocuments(revision, GeminiDocument.class, defaultResourceTypes);
        System.out.println(ids);

        //Then
        assertThat(ids.contains("uid"), is(true));
    }

    @Test
    public void checkThatSkipsUnreadableDocuments() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        //Given
        String revision = "revision";
        String id = "mydoc id";
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList(id));
        when(documentBundleReader.readBundle(id, revision)).thenThrow(new UnknownContentTypeException("Unreadable"));

        //When
        List<String> ids = service.getPublicDocuments(revision, GeminiDocument.class, defaultResourceTypes);

        //Then
        assertTrue(ids.isEmpty());
    }

    @Test
    public void logsTheCauseWhenADocumentCannotBeRead() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        //Given
        String revision = "revision";
        String id = "broken doc id";
        RuntimeException cause = new IllegalStateException("Resource busy");
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList(id));
        when(documentBundleReader.readBundle(id, revision)).thenThrow(cause);

        //When
        service.getPublicDocuments(revision, GeminiDocument.class, defaultResourceTypes);

        //Then — the swallowed exception must be attached to the log event, not dropped,
        // otherwise production logs cannot explain why a document failed to read.
        List<ILoggingEvent> errors = logAppender.list.stream()
            .filter(e -> e.getLevel() == Level.ERROR)
            .toList();
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0).getFormattedMessage(), is("Failed to read " + id + " @ " + revision));
        assertThat(errors.get(0).getThrowableProxy().getMessage(), is("Resource busy"));
    }

    @Test
    public void checkThatOnlyReadsDocumentsOfCorrectType() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        //Given
        String revision = "revision";
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList("uid"));

        Class differentMetadataType = mock(MetadataDocument.class).getClass();

        //When
        List<String> ids = service.getPublicDocuments(revision, differentMetadataType, defaultResourceTypes);

        //Then
        assertTrue(ids.isEmpty());
    }

    @Test
    @SneakyThrows
    public void checkThatOnlyReadsUserAccessibleDocuments() {
        //Given
        String id = "id";
        String revision = "revision";
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList(id));
        MetadataInfo metadata = MetadataInfo.builder().build();
        MetadataDocument document = new GeminiDocument().setId(id).setMetadata(metadata);
        when(documentBundleReader.readBundle(id, revision)).thenReturn(document);

        //When
        List<String> ids = service.getPublicDocuments(revision, GeminiDocument.class, defaultResourceTypes);

        //Then
        assertTrue(ids.isEmpty());
        verify(documentBundleReader).readBundle(id, revision);
    }

    @Test
    public void checksThatTypeIsMatched() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        //Given
        String id = "a";
        String resourceType = "Dataset";
        String revision = "revision";
        Multimap<Permission, String> permissions = HashMultimap.create();
        permissions.put(Permission.VIEW, "public");
        MetadataInfo metadata = MetadataInfo.builder().permissions(permissions).catalogue("eidc").state("published").build();
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList(id));
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getId()).thenReturn(id);
        when(document.getType()).thenReturn(resourceType);
        when(document.getMetadata()).thenReturn(metadata);
        when(document.getCatalogue()).thenReturn("eidc");
        when(documentBundleReader.readBundle("a", revision)).thenReturn(document);

        //When
        List<String> publicIds = service.getPublicDocuments(revision, GeminiDocument.class,Arrays.asList(resourceType));

        //Then
        assertTrue(publicIds.size() == 1);
        verify(documentBundleReader).readBundle("a", revision);
    }

    @Test
    @SneakyThrows
    public void checksThatTypeIsNotMatched() {
        //Given
        String id = "a";
        String documentResourceType = "A_N_Other";
        String geminiResourceType = "Dataset";
        String revision = "revision";
        Multimap<Permission, String> permissions = HashMultimap.create();
        permissions.put(Permission.VIEW, "public");
        MetadataInfo metadata = MetadataInfo.builder().permissions(permissions).build();
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList(id));
        MetadataDocument document = new GeminiDocument().setId(id).setType(documentResourceType).setMetadata(metadata);
        when(documentBundleReader.readBundle("a", revision)).thenReturn(document);

        //When
        List<String> publicIds = service.getPublicDocuments(revision, GeminiDocument.class, Arrays.asList(geminiResourceType));

        //Then
        assertTrue(publicIds.isEmpty());
        verify(documentBundleReader).readBundle("a", revision);
    }

    @Test
    public void checksThatTypeIsCaseInsensitive() throws IOException, UnknownContentTypeException, DataRepositoryException, PostProcessingException {
        //Given
        String id = "a";
        String documentResourceType = "dataset";
        String geminiResourceType = "DATASET";
        String revision = "revision";
        Multimap<Permission, String> permissions = HashMultimap.create();
        permissions.put(Permission.VIEW, "public");
        MetadataInfo metadata = MetadataInfo.builder().permissions(permissions).catalogue("eidc").state("published").build();
        when(listingService.filterFilenames(any(List.class))).thenReturn(Arrays.asList(id));
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getId()).thenReturn(id);
        when(document.getType()).thenReturn(documentResourceType);
        when(document.getMetadata()).thenReturn(metadata);
        when(document.getCatalogue()).thenReturn("eidc");
        when(documentBundleReader.readBundle("a", revision)).thenReturn(document);

        //When
        List<String> publicIds = service.getPublicDocuments(revision, GeminiDocument.class, Arrays.asList(geminiResourceType));

        //Then
        assertTrue(publicIds.size() == 1);
        verify(documentBundleReader).readBundle("a", revision);
    }

    @Test
    @SneakyThrows
    public void checkOnlyEidcDocumentListed() {
        //given
        String revision = "revision";
        MetadataDocument document = new GeminiDocument()
            .setId("test")
            .setMetadata(
                MetadataInfo.builder().catalogue("ceh").build()
            );

        //when
        List<String> actual = service.getPublicDocuments(revision, GeminiDocument.class, defaultResourceTypes);

        //then
        assertThat("should be no items in list", actual.size(), equalTo(0));
    }
}
