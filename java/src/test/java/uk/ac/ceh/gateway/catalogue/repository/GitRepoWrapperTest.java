package uk.ac.ceh.gateway.catalogue.repository;

import com.google.common.eventbus.EventBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.io.OutputStream;
import java.util.List;

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
        //Given the current per-document token is "metaRev1:rawRev1"
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        MetadataInfo info = MetadataInfo.builder().build();
        DataWriter writer = out -> {};
        MetadataDocument submitted = mock(MetadataDocument.class);

        givenCurrentRevisions("metaRev1", "rawRev1");

        DataOngoingCommit commit = mock(DataOngoingCommit.class);
        DataRevision<CatalogueUser> newRev = mock(DataRevision.class);
        given(repo.submitData(eq("doc1.meta"), any())).willReturn(commit);
        given(commit.submitData(eq("doc1.raw"), any())).willReturn(commit);
        given(commit.commit(any(), any())).willReturn(newRev);
        given(newRev.getRevisionID()).willReturn("rev2");

        //When the caller's expected token matches
        repoWrapper.save(user, "doc1", "msg", info, writer, "metaRev1:rawRev1", submitted);

        //Then the commit is performed
        verify(commit).commit(user, "msg");
    }

    @Test
    public void rejectsWhenExpectedRevisionIsStale() throws Exception {
        //Given the document has moved on to "metaRev2:rawRev2" but the editor loaded "metaRev1:rawRev1"
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        MetadataInfo info = MetadataInfo.builder().build();
        DataWriter writer = out -> {};
        MetadataDocument submitted = mock(MetadataDocument.class);

        givenCurrentRevisions("metaRev2", "rawRev2");

        //When/Then a conflict is raised and nothing is committed
        MetadataConflictException ex = assertThrows(MetadataConflictException.class, () ->
            repoWrapper.save(user, "doc1", "msg", info, writer, "metaRev1:rawRev1", submitted));
        assertThat(ex.getSubmittedDocument(), is(sameInstance(submitted)));
        verify(repo, never()).submitData(any(), any());
    }

    /**
     * Regression: a plain content edit changes {@code .raw} but rewrites {@code .meta} byte-identical,
     * so {@code git log -- <id>.meta} does not advance. A token read from {@code .meta} alone would still
     * match and the concurrent edit would be silently overwritten — the exact lost update issue #134
     * describes. The token must include the {@code .raw} revision for this to be caught.
     */
    @Test
    public void rejectsWhenOnlyTheDocumentBodyMovedOn() throws Exception {
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        MetadataInfo info = MetadataInfo.builder().build();
        DataWriter writer = out -> {};
        MetadataDocument submitted = mock(MetadataDocument.class);

        // .meta is where the editor left it; only .raw has moved on
        givenCurrentRevisions("metaRev1", "rawRev2");

        assertThrows(MetadataConflictException.class, () ->
            repoWrapper.save(user, "doc1", "msg", info, writer, "metaRev1:rawRev1", submitted));
        verify(repo, never()).submitData(any(), any());
    }

    private void givenCurrentRevisions(String metaRevision, String rawRevision) throws Exception {
        DataRevision<CatalogueUser> meta = mock(DataRevision.class);
        given(meta.getRevisionID()).willReturn(metaRevision);
        given(repo.getRevisions("doc1.meta")).willReturn(List.of(meta));

        DataRevision<CatalogueUser> raw = mock(DataRevision.class);
        given(raw.getRevisionID()).willReturn(rawRevision);
        given(repo.getRevisions("doc1.raw")).willReturn(List.of(raw));
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
