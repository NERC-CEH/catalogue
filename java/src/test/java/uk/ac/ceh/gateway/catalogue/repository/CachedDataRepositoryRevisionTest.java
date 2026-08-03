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

        String aBefore = cached.getDocumentRevisionToken("docA");
        assertThat(aBefore, is(notNullValue()));

        // When an UNRELATED document (docB) is committed
        commit(repo, "docB", "v2");

        // Then docA's token is unchanged (per-document, not repo-wide)
        assertThat(cached.getDocumentRevisionToken("docA"), is(aBefore));

        // And when docA itself changes, its token moves
        commit(repo, "docA", "v2");
        assertThat(cached.getDocumentRevisionToken("docA"), is(not(aBefore)));
    }

    @Test
    public void tokenMovesWhenBothFilesChangeInOneCommit(@TempDir Path tmp) throws Exception {
        DataRepository<CatalogueUser> repo = gitRepo(tmp.toFile());
        commit(repo, "docA", "v1");
        CachedDataRepository cached = new CachedDataRepository(repo);
        String before = cached.getDocumentRevisionToken("docA");

        // commit() writes both .meta and .raw in one commit, as GitRepoWrapper.save does
        commit(repo, "docA", "v2");

        assertThat(cached.getDocumentRevisionToken("docA"), is(not(before)));
    }

    /**
     * The realistic editor save: the document body ({@code .raw}) changes but the {@code MetadataInfo}
     * ({@code .meta}) is rewritten byte-identical, because editing a title does not touch permissions,
     * catalogue or state. Git is content-addressed and {@code getRevisions} is {@code git log -- <path>},
     * which applies ANY_DIFF history simplification — so an unchanged {@code .meta} blob means the commit
     * never appears in that path's log. A token read from {@code .meta} alone therefore does NOT move,
     * and the optimistic lock fails open on exactly the case issue #134 describes.
     */
    @Test
    public void tokenMovesWhenOnlyTheDocumentBodyChanges(@TempDir Path tmp) throws Exception {
        DataRepository<CatalogueUser> repo = gitRepo(tmp.toFile());
        commitRawOnlyChange(repo, "docA", "v1");
        CachedDataRepository cached = new CachedDataRepository(repo);
        String before = cached.getDocumentRevisionToken("docA");

        // A content-only edit: .raw changes, .meta is rewritten with identical bytes
        commitRawOnlyChange(repo, "docA", "v2");

        assertThat(cached.getDocumentRevisionToken("docA"), is(not(before)));
    }

    @Test
    public void tokenMovesWhenOnlyThePermissionsChange(@TempDir Path tmp) throws Exception {
        DataRepository<CatalogueUser> repo = gitRepo(tmp.toFile());
        commitRawOnlyChange(repo, "docA", "v1");
        CachedDataRepository cached = new CachedDataRepository(repo);
        String before = cached.getDocumentRevisionToken("docA");

        // A permissions-only edit: .meta changes, .raw is untouched
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        repo.submitData("docA.meta", o -> o.write("meta-v2".getBytes()))
            .commit(user, "permissions of docA changed");

        assertThat(cached.getDocumentRevisionToken("docA"), is(not(before)));
    }

    @Test
    public void tokenIsUnaffectedByChangesToOtherDocuments(@TempDir Path tmp) throws Exception {
        DataRepository<CatalogueUser> repo = gitRepo(tmp.toFile());
        commitRawOnlyChange(repo, "docA", "v1");
        commitRawOnlyChange(repo, "docB", "v1");
        CachedDataRepository cached = new CachedDataRepository(repo);
        String before = cached.getDocumentRevisionToken("docA");

        commitRawOnlyChange(repo, "docB", "v2");

        assertThat(cached.getDocumentRevisionToken("docA"), is(before));
    }

    /**
     * A commit that rewrites both halves with identical bytes changes nothing a user could have edited,
     * so the token must not move — otherwise an unrelated republish would spuriously 409 every open
     * editor. The token is a digest of content, so this holds by construction.
     */
    @Test
    public void tokenIsStableAcrossANoOpRewrite(@TempDir Path tmp) throws Exception {
        DataRepository<CatalogueUser> repo = gitRepo(tmp.toFile());
        commit(repo, "docA", "v1");
        CachedDataRepository cached = new CachedDataRepository(repo);
        String before = cached.getDocumentRevisionToken("docA");

        // Same bytes, committed again alongside a change to an unrelated document so the commit is real
        commit(repo, "docA", "v1");
        commit(repo, "docB", "v1");

        assertThat(cached.getDocumentRevisionToken("docA"), is(before));
    }

    /**
     * A document being created has neither blob at HEAD. That is a legitimate state, not an error: the
     * token must resolve rather than throw, or creating a new record fails.
     */
    @Test
    public void tokenResolvesForADocumentThatDoesNotExistYet(@TempDir Path tmp) throws Exception {
        DataRepository<CatalogueUser> repo = gitRepo(tmp.toFile());
        commit(repo, "docA", "v1");
        CachedDataRepository cached = new CachedDataRepository(repo);

        assertThat(cached.getDocumentRevisionToken("brandNew"), is("-:-"));
    }

    /** Writes a changing {@code .raw} alongside a constant {@code .meta}, as a real content edit does. */
    private void commitRawOnlyChange(DataRepository<CatalogueUser> repo, String id, String body) throws Exception {
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        repo.submitData(id + ".meta", o -> o.write("meta-constant".getBytes()))
            .submitData(id + ".raw", o -> o.write(("raw-" + body).getBytes()))
            .commit(user, "commit " + id + " " + body);
    }
}
