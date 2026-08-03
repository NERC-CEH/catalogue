package uk.ac.ceh.gateway.catalogue.repository;

import com.google.common.eventbus.EventBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataOngoingCommit;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.datastore.DataWriter;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataConflictException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.FacilityEventService;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class GitRepoWrapperTest {
    @Mock private DataRepository<CatalogueUser> repo;
    @Mock private DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    @Mock private BundledReaderService<MetadataDocument> bundledReader;
    @Mock private EventBus eventBus;
    @Mock private FacilityEventService facilityEventService;

    @InjectMocks private GitRepoWrapper repoWrapper;

    @Test
    public void canSave() throws DataRepositoryException {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        String id = "test";
        String message = "template: test";
        MetadataInfo metadataInfo = MetadataInfo.builder().build();
        DataWriter dataWriter = (OutputStream out) -> {
            throw new UnsupportedOperationException("Not supported yet.");
        };
        DataOngoingCommit dataOngoingCommit = mock(DataOngoingCommit.class);
        DataRevision<CatalogueUser> revision = mock(DataRevision.class);

        given(repo.submitData(eq("test.meta"), any())).willReturn(dataOngoingCommit);
        given(dataOngoingCommit.submitData("test.raw", dataWriter)).willReturn(dataOngoingCommit);
        given(dataOngoingCommit.commit(user, "template: test")).willReturn(revision);
        given(revision.getRevisionID()).willReturn("rev123");

        //When
        repoWrapper.save(user, id, message, metadataInfo, dataWriter);

        //Then
        verify(dataOngoingCommit).commit(user, "template: test");
        // The post-commit facility read must happen at the commit's revision, not the cache-stale latest
        verify(facilityEventService).getMonitoringFacility(id, "rev123");
    }

    @Test
    public void savesWhenExpectedRevisionMatchesCurrent() throws Exception {
        //Given a document whose current token the editor still holds
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        MetadataInfo info = MetadataInfo.builder().build();
        DataWriter writer = out -> {};
        MetadataDocument submitted = mock(MetadataDocument.class);

        givenCurrentContent("meta-v1", "raw-v1");
        String held = CachedDataRepository.revisionToken(repo, "doc1");

        DataOngoingCommit commit = mock(DataOngoingCommit.class);
        DataRevision<CatalogueUser> newRev = mock(DataRevision.class);
        given(repo.submitData(eq("doc1.meta"), any())).willReturn(commit);
        given(commit.submitData(eq("doc1.raw"), any())).willReturn(commit);
        given(commit.commit(any(), any())).willReturn(newRev);
        given(newRev.getRevisionID()).willReturn("rev2");

        //When the caller's expected token matches
        repoWrapper.save(user, "doc1", "msg", info, writer, held, submitted);

        //Then the commit is performed
        verify(commit).commit(user, "msg");
    }

    @Test
    public void rejectsWhenExpectedRevisionIsStale() throws Exception {
        //Given the editor loaded one version but both halves have since moved on
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        MetadataInfo info = MetadataInfo.builder().build();
        DataWriter writer = out -> {};
        MetadataDocument submitted = mock(MetadataDocument.class);

        givenCurrentContent("meta-v1", "raw-v1");
        String held = CachedDataRepository.revisionToken(repo, "doc1");
        givenCurrentContent("meta-v2", "raw-v2");

        //When/Then a conflict is raised and nothing is committed
        MetadataConflictException ex = assertThrows(MetadataConflictException.class, () ->
            repoWrapper.save(user, "doc1", "msg", info, writer, held, submitted));
        assertThat(ex.getSubmittedDocument(), is(sameInstance(submitted)));
        verify(repo, never()).submitData(any(), any());
    }

    /**
     * Regression: a plain content edit changes {@code .raw} while rewriting {@code .meta}
     * byte-identical, because editing a title touches neither permissions nor state. A token derived
     * from {@code .meta} alone would still match, and the concurrent edit would be silently
     * overwritten — the lost update issue #134 describes. The token spans both halves, so a change to
     * {@code .raw} alone is still caught.
     */
    @Test
    public void rejectsWhenOnlyTheDocumentBodyMovedOn() throws Exception {
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        MetadataInfo info = MetadataInfo.builder().build();
        DataWriter writer = out -> {};
        MetadataDocument submitted = mock(MetadataDocument.class);

        givenCurrentContent("meta-constant", "raw-v1");
        String held = CachedDataRepository.revisionToken(repo, "doc1");
        // .meta is byte-identical to what the editor loaded; only .raw has moved on
        givenCurrentContent("meta-constant", "raw-v2");

        assertThrows(MetadataConflictException.class, () ->
            repoWrapper.save(user, "doc1", "msg", info, writer, held, submitted));
        verify(repo, never()).submitData(any(), any());
    }

    /**
     * Stubs {@code doc1}'s two blobs at HEAD. The lock token is a digest of their content, so the
     * bodies here are what the token is computed from — call
     * {@link CachedDataRepository#revisionToken} to read back the token they produce.
     */
    private void givenCurrentContent(String metaBody, String rawBody) throws Exception {
        // Build both blobs before stubbing: blob() stubs a mock of its own, and Mockito rejects that
        // being set up inside the argument to willReturn() while this stubbing is still open.
        DataDocument meta = blob(metaBody);
        DataDocument raw = blob(rawBody);
        given(repo.getData("doc1.meta")).willReturn(meta);
        given(repo.getData("doc1.raw")).willReturn(raw);
    }

    /** A blob handing out a fresh stream per call, so the content can be digested more than once. */
    private DataDocument blob(String body) throws Exception {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        DataDocument document = mock(DataDocument.class);
        given(document.getInputStream()).willAnswer(invocation -> new ByteArrayInputStream(bytes));
        return document;
    }

    @Test
    public void canDelete() throws DataRepositoryException {
        //Given
        DataOngoingCommit dataOngoingCommit = mock(DataOngoingCommit.class);
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        String id = "test";

        given(repo.deleteData("test.meta")).willReturn(dataOngoingCommit);
        given(dataOngoingCommit.deleteData("test.raw")).willReturn(dataOngoingCommit);

        //When
        repoWrapper.delete(user, id);

        //Then
        verify(dataOngoingCommit).commit(user, "delete document: test");
    }

}
