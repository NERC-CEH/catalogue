package uk.ac.ceh.gateway.catalogue.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.git.GitFileNotFoundException;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Read-through cache in front of the Git {@link DataRepository}.
 *
 * <p>The production datastore lives on a remote SMB-mounted SAN, where every filesystem operation
 * is a network round-trip. A single record read resolves HEAD then opens the {@code .meta} and
 * {@code .raw} blobs — each doing a revision-resolve, tree-walk and blob-open. This class caches
 * the <em>immutable byte content</em> of those blobs (and the HEAD revision id) so repeat reads of
 * the same record avoid the datastore entirely.</p>
 *
 * <p>Caching the bytes (not the assembled {@code MetadataDocument}) is deliberate: callers mutate
 * the document they get back, so {@code MetadataInfoBundledReaderService} still rebuilds a fresh
 * document on every read — only the I/O is cached.</p>
 *
 * <p>Eviction lives on {@link GitRepoWrapper#save} / {@link GitRepoWrapper#delete} (the write path),
 * which name these same caches. This bean is intentionally separate from {@code GitRepoWrapper} to
 * avoid a circular dependency (GitRepoWrapper → FacilityEventService → BundledReaderService).</p>
 */
@Slf4j
@Service
public class CachedDataRepository {
    public static final String REVISION_ID_CACHE = "datastore-revision-id";
    public static final String LATEST_CACHE = "datastore-latest";
    public static final String HISTORICAL_CACHE = "datastore-historical";
    public static final String DOC_REVISION_CACHE = "datastore-doc-revision";

    /** Stands in for a half of a document that does not exist at HEAD; see {@code contentDigest}. */
    private static final String ABSENT = "-";

    private final DataRepository<CatalogueUser> repo;

    public CachedDataRepository(DataRepository<CatalogueUser> repo) {
        this.repo = repo;
        log.info("Creating");
    }

    /**
     * The current HEAD revision id. Single-entry cache; evicted on every save/delete so the next
     * read picks up the new commit.
     */
    @Cacheable(value = REVISION_ID_CACHE, key = "'HEAD'")
    public String getLatestRevisionId() throws IOException {
        return repo.getLatestRevision().getRevisionID();
    }

    /**
     * Blob content at HEAD, keyed by file name only (the latest content for a given name is what
     * matters; the {@code revision} argument is just what we fetch with on a miss). Evicted per-file
     * when that document is saved or deleted.
     */
    @Cacheable(value = LATEST_CACHE, key = "#name")
    public byte[] readLatest(String revision, String name) throws IOException {
        return toBytes(repo.getData(revision, name));
    }

    /**
     * Blob content at an explicit historical revision. Content at a given revision is immutable, so
     * this is keyed by {@code revision:name} and never needs eviction (bounded only by size/TTL).
     */
    @Cacheable(value = HISTORICAL_CACHE, key = "#revision + ':' + #name")
    public byte[] readAtRevision(String revision, String name) throws IOException {
        return toBytes(repo.getData(revision, name));
    }

    /**
     * The per-document token used for optimistic locking. Unlike {@link #getLatestRevisionId()}
     * (repo-wide HEAD) this only moves when <em>this</em> document changes, so an unrelated save does not
     * trip a conflict.
     *
     * <p>A document is two blobs, and an edit may touch either one independently: a content edit rewrites
     * {@code .raw} while leaving {@code .meta} byte-identical, and a permissions edit does the reverse.
     * The token is a digest of both, so a change to either half changes the whole.</p>
     *
     * <p><strong>Why content and not the commit id.</strong> The obvious token — the newest commit
     * touching each path — costs {@code repo.getRevisions(name)}, which is {@code git log -- <path>}: it
     * walks the entire commit graph from HEAD with path filtering and materialises every matching commit,
     * only for the caller to keep the first. Against the SMB-mounted SAN that is thousands of round-trips
     * per call, on both the read path (every document GET) and the write path (every save, twice, inside
     * {@code GitRepoWrapper}'s {@code synchronized} block). Hashing the blobs instead costs a HEAD
     * resolve, a tree lookup and a blob open per file — proportional to tree depth, not to history
     * length.</p>
     *
     * <p>Content is also the <em>more correct</em> signal. {@code git log -- <path>} applies ANY_DIFF
     * history simplification, so a commit that rewrites a path with identical bytes produces no diff for
     * it and never appears in that path's log; a commit-id token therefore silently fails to move for
     * some edits, and an optimistic lock whose token does not move fails <em>open</em>. A digest moves
     * exactly when the bytes a user could have edited differ, which is the question the lock is asking.</p>
     *
     * <p>Cached by document id and evicted on save/delete, mirroring {@link #readLatest}. Callers on the
     * write path must not use this cached value for their compare-then-commit check — they need an
     * authoritative fresh read; see {@code GitRepoWrapper.currentDocumentRevision}.</p>
     *
     * @param documentId the file id without extension, including any folder prefix
     *                   (e.g. {@code abc-123} or {@code service-agreement/abc-123})
     */
    @Cacheable(value = DOC_REVISION_CACHE, key = "#documentId")
    public String getDocumentRevisionToken(String documentId) throws IOException {
        return revisionToken(repo, documentId);
    }

    /**
     * Computes the token of {@link #getDocumentRevisionToken} directly against the datastore, bypassing
     * the cache. Shared with the write path, which must compare against an authoritative current value —
     * {@code repo.getData(name)} resolves HEAD on every call, so this always reflects the committed state.
     */
    public static String revisionToken(DataRepository<CatalogueUser> repo, String documentId) throws DataRepositoryException {
        return contentDigest(repo, documentId + ".meta") + ":" + contentDigest(repo, documentId + ".raw");
    }

    /**
     * A digest of {@code name}'s content at HEAD, or {@link #ABSENT} when the blob does not exist there —
     * a document mid-creation has neither half, and only one half is written by a {@code .meta}-only
     * permissions commit against a document whose {@code .raw} predates it. Absence is a legitimate state
     * that must produce a stable token rather than an error, but <em>only</em> absence: any other datastore
     * failure propagates, because silently returning a token here would let the lock compare equal and
     * fail open.
     */
    private static String contentDigest(DataRepository<CatalogueUser> repo, String name) throws DataRepositoryException {
        DataDocument document;
        try {
            document = repo.getData(name);
        } catch (GitFileNotFoundException absent) {
            return ABSENT;
        }
        try {
            return HexFormat.of().formatHex(sha256().digest(toBytes(document)));
        } catch (IOException ex) {
            throw new DataRepositoryException(ex);
        }
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required of every JVM", ex);
        }
    }

    /**
     * Evict the cached blob content and HEAD revision id for a document after a write that commits
     * <em>directly</em> to the datastore, bypassing {@link GitRepoWrapper} (the service-agreement
     * write path does this). Mirrors the eviction {@link GitRepoWrapper#save} performs: both blob
     * names for the document, plus the shared HEAD id — which every commit advances, so it must be
     * cleared regardless of which document changed, or all "latest" reads resolve at a stale
     * revision.
     *
     * <p>Callers must invoke this on the {@code CachedDataRepository} bean (not via a self-call from
     * within their own bean) so the cache proxy actually applies the eviction.</p>
     *
     * @param documentId the file id including any folder prefix, e.g. {@code service-agreement/<id>}
     */
    @Caching(evict = {
        @CacheEvict(value = LATEST_CACHE, key = "#documentId + '.meta'"),
        @CacheEvict(value = LATEST_CACHE, key = "#documentId + '.raw'"),
        @CacheEvict(value = DOC_REVISION_CACHE, key = "#documentId"),
        @CacheEvict(value = REVISION_ID_CACHE, allEntries = true)
    })
    public void evictAfterDirectWrite(String documentId) {
        log.debug("Evicting caches after direct datastore write to {}", documentId);
    }

    private static byte[] toBytes(DataDocument document) throws IOException {
        try (InputStream stream = document.getInputStream()) {
            return stream.readAllBytes();
        }
    }
}
