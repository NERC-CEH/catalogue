package uk.ac.ceh.gateway.catalogue.repository;

import com.google.common.eventbus.EventBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.git.GitDataRepository;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.io.File;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CachedDataRepositoryRevisionTest {

    private DataRepository<CatalogueUser> gitRepo(File dir) throws Exception {
        return new GitDataRepository<>(dir, CatalogueUser::new, new EventBus());
    }

    private void commit(DataRepository<CatalogueUser> repo, String id, String body) throws Exception {
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        repo.submitData(id + ".meta", o -> o.write(("meta-" + body).getBytes()))
            .submitData(id + ".raw", o -> o.write(("raw-" + body).getBytes()))
            .commit(user, "commit " + id + " " + body);
    }

    @Test
    public void tokenChangesOnlyWhenThisDocumentChanges(@TempDir Path tmp) throws Exception {
        // Given a real git-backed store with two documents committed
        DataRepository<CatalogueUser> repo = gitRepo(tmp.toFile());
        commit(repo, "docA", "v1");
        commit(repo, "docB", "v1");
        CachedDataRepository cached = new CachedDataRepository(repo);

        String aBefore = cached.getDocumentRevisionId("docA.meta");
        assertThat(aBefore, is(notNullValue()));

        // When an UNRELATED document (docB) is committed
        commit(repo, "docB", "v2");

        // Then docA's token is unchanged (per-document, not repo-wide)
        assertThat(cached.getDocumentRevisionId("docA.meta"), is(aBefore));

        // And when docA itself changes, its token moves
        commit(repo, "docA", "v2");
        assertThat(cached.getDocumentRevisionId("docA.meta"), is(not(aBefore)));
    }

    @Test
    public void metaTokenReflectsRawOnlyChangeSinceBothCommitTogether(@TempDir Path tmp) throws Exception {
        DataRepository<CatalogueUser> repo = gitRepo(tmp.toFile());
        commit(repo, "docA", "v1");
        CachedDataRepository cached = new CachedDataRepository(repo);
        String before = cached.getDocumentRevisionId("docA.meta");

        // commit() writes both .meta and .raw in one commit, as GitRepoWrapper.save does
        commit(repo, "docA", "v2");

        assertThat(cached.getDocumentRevisionId("docA.meta"), is(not(before)));
    }
}
