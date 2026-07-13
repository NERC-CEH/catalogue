package uk.ac.ceh.gateway.catalogue.repository;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.io.IOException;
import java.io.InputStream;

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
        @CacheEvict(value = REVISION_ID_CACHE, allEntries = true)
    })
    public void evictAfterDirectWrite(String documentId) {
        log.debug("Evicting caches after direct datastore write to {}", documentId);
    }

    @SneakyThrows
    private byte[] toBytes(DataDocument document) {
        try (InputStream stream = document.getInputStream()) {
            return stream.readAllBytes();
        }
    }
}
