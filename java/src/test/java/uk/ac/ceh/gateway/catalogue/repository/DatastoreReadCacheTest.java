package uk.ac.ceh.gateway.catalogue.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.datastore.DataWriter;
import uk.ac.ceh.components.datastore.git.GitFileNotFoundException;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.services.FacilityEventService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Verifies the datastore read-through cache behaviour: repeat reads hit the cache (no datastore I/O)
 * and a write through {@link GitRepoWrapper} evicts so the next read re-fetches. Uses a real Spring
 * caching context (a plain ConcurrentMapCacheManager) rather than the "test" profile, which disables
 * caching.
 */
@SpringJUnitConfig(DatastoreReadCacheTest.Config.class)
public class DatastoreReadCacheTest {

    @EnableCaching
    @Configuration
    static class Config {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(
                CachedDataRepository.REVISION_ID_CACHE,
                CachedDataRepository.LATEST_CACHE,
                CachedDataRepository.HISTORICAL_CACHE,
                CachedDataRepository.DOC_REVISION_CACHE);
        }

        @Bean
        @SuppressWarnings("unchecked")
        DataRepository<CatalogueUser> dataRepository() {
            return mock(DataRepository.class, RETURNS_DEEP_STUBS);
        }

        @Bean
        CachedDataRepository cachedDataRepository(DataRepository<CatalogueUser> repo) {
            return new CachedDataRepository(repo);
        }

        @Bean
        @SuppressWarnings("unchecked")
        DocumentInfoMapper<MetadataInfo> documentInfoMapper() {
            return mock(DocumentInfoMapper.class);
        }

        @Bean
        FacilityEventService facilityEventService() {
            return mock(FacilityEventService.class);
        }

        @Bean
        GitRepoWrapper gitRepoWrapper(
            DataRepository<CatalogueUser> repo,
            DocumentInfoMapper<MetadataInfo> documentInfoMapper,
            FacilityEventService facilityEventService
        ) {
            return new GitRepoWrapper(repo, documentInfoMapper, facilityEventService);
        }
    }

    @Autowired DataRepository<CatalogueUser> repo;
    @Autowired CachedDataRepository cachedRepo;
    @Autowired GitRepoWrapper gitRepoWrapper;
    @Autowired CacheManager cacheManager;

    @BeforeEach
    public void resetState() {
        // The Spring context (and its singleton mock + caches) is reused across test methods, so
        // fully reset the mock (clears both invocation counts AND stubbing — e.g. a willThrow set by
        // one test must not leak into the next) and clear the caches to isolate each test.
        reset(repo);
        for (String name : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) cache.clear();
        }
    }

    private static DataDocument blob(String content) {
        DataDocument doc = mock(DataDocument.class);
        try {
            // fresh stream per call so re-fetch after eviction is not reading a consumed stream
            given(doc.getInputStream()).willAnswer(i -> new ByteArrayInputStream(content.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return doc;
    }

    @Test
    public void secondLatestReadHitsCacheAndDoesNotTouchDatastore() throws Exception {
        given(repo.getData("rev", "abc.meta")).willAnswer(i -> blob("meta"));

        cachedRepo.readLatest("rev", "abc.meta");
        cachedRepo.readLatest("rev", "abc.meta");

        verify(repo, times(1)).getData("rev", "abc.meta");
    }

    @Test
    public void historicalReadIsCachedByRevisionAndName() throws Exception {
        given(repo.getData("rev1", "abc.meta")).willAnswer(i -> blob("v1"));

        cachedRepo.readAtRevision("rev1", "abc.meta");
        cachedRepo.readAtRevision("rev1", "abc.meta");

        verify(repo, times(1)).getData("rev1", "abc.meta");
    }

    @Test
    public void saveEvictsLatestEntriesSoNextReadRefetches() throws Exception {
        given(repo.getData("rev", "abc.meta")).willAnswer(i -> blob("meta"));

        cachedRepo.readLatest("rev", "abc.meta"); // miss -> getData #1
        cachedRepo.readLatest("rev", "abc.meta"); // hit

        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        DataWriter writer = out -> { };
        gitRepoWrapper.save(user, "abc", "msg", MetadataInfo.builder().build(), writer);

        cachedRepo.readLatest("rev", "abc.meta"); // evicted -> getData #2

        verify(repo, times(2)).getData("rev", "abc.meta");
    }

    /**
     * The per-document revision token (the ETag source for optimistic locking) is cached by
     * {@link CachedDataRepository#getDocumentRevisionId} and must be evicted on write, exactly like the
     * blob-content cache above. Without this eviction the same stale ETag is served to every browser for
     * the whole {@code DOC_REVISION_CACHE} TTL (6h in production), so a second save would spuriously 409.
     * This drives the token read -> save -> token read cycle through the real {@code @EnableCaching}
     * proxy to prove the eviction actually fires.
     */
    @Test
    public void saveEvictsDocRevisionSoNextTokenReadRefetches() throws Exception {
        given(repo.getRevisions("abc.meta")).willAnswer(i -> List.of(revision("rev1")));

        cachedRepo.getDocumentRevisionId("abc.meta"); // miss -> getRevisions #1
        cachedRepo.getDocumentRevisionId("abc.meta"); // hit

        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        DataWriter writer = out -> { };
        gitRepoWrapper.save(user, "abc", "msg", MetadataInfo.builder().build(), writer);

        cachedRepo.getDocumentRevisionId("abc.meta"); // evicted -> getRevisions #2

        verify(repo, times(2)).getRevisions("abc.meta");
    }

    @SuppressWarnings("unchecked")
    private static DataRevision<CatalogueUser> revision(String id) {
        DataRevision<CatalogueUser> rev = mock(DataRevision.class);
        given(rev.getRevisionID()).willReturn(id);
        return rev;
    }

    @Test
    public void deleteEvictsLatestEntries() throws Exception {
        given(repo.getData("rev", "abc.raw")).willAnswer(i -> blob("raw"));

        cachedRepo.readLatest("rev", "abc.raw");
        cachedRepo.readLatest("rev", "abc.raw");

        gitRepoWrapper.delete(new CatalogueUser("test", "test@ceh.ac.uk"), "abc");

        cachedRepo.readLatest("rev", "abc.raw");

        verify(repo, times(2)).getData("rev", "abc.raw");
    }

    /**
     * Regression: a missing blob must surface as the original (checked) {@link GitFileNotFoundException},
     * never wrapped in a {@code java.lang.reflect.UndeclaredThrowableException}.
     *
     * <p>The cache methods declare {@code throws IOException} rather than relying on Lombok
     * {@code @SneakyThrows}. Were the {@code throws} clause dropped, the CGLIB cache proxy would have no
     * checked exception to declare and would wrap the escaping checked exception in an
     * {@code UndeclaredThrowableException} (a {@code RuntimeException}). That defeats the
     * {@code catch (IOException) … instanceof GitFileNotFoundException} guards in {@code FacilityEventService}
     * and {@code JenaIndexingService} — which is what caused new-document creates (a read of the
     * not-yet-committed id) to fail with a 404.
     */
    @Test
    public void missingBlobPropagatesGitFileNotFoundUnwrapped() throws Exception {
        given(repo.getData("rev", "missing.meta"))
            .willThrow(new GitFileNotFoundException("no such file"));

        Throwable thrown = assertThrows(Throwable.class,
            () -> cachedRepo.readLatest("rev", "missing.meta"));

        assertInstanceOf(GitFileNotFoundException.class, thrown,
            "cache proxy must propagate the bare GitFileNotFoundException, not wrap it");
        assertInstanceOf(IOException.class, thrown,
            "callers catch IOException, so the propagated exception must be an IOException");
    }

    /**
     * Regression: a commit failure in {@link GitRepoWrapper#save} must surface as the original (checked)
     * {@link DataRepositoryException}, never wrapped in {@code UndeclaredThrowableException}.
     *
     * <p>{@code save} is {@code @CacheEvict}-advised, so it runs behind a CGLIB proxy here (real
     * {@code @EnableCaching} context, not the "test" profile which disables caching). If {@code save}
     * relied on {@code @SneakyThrows} instead of declaring {@code throws DataRepositoryException}, the
     * proxy would have no checked exception to declare and would wrap the escaping
     * {@code DataRepositoryException} thrown by {@code commit(...)} — defeating the
     * {@code catch (DataRepositoryException)} / {@code catch (IOException)} guards in
     * {@code GitDocumentRepository} that translate it into a user-facing {@code DocumentRepositoryException}.
     */
    @Test
    public void saveCommitFailurePropagatesDataRepositoryExceptionUnwrapped() throws Exception {
        given(repo.submitData(anyString(), any()).submitData(anyString(), any()).commit(any(), anyString()))
            .willThrow(new DataRepositoryException("commit failed"));

        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        DataWriter writer = out -> { };

        Throwable thrown = assertThrows(Throwable.class,
            () -> gitRepoWrapper.save(user, "abc", "msg", MetadataInfo.builder().build(), writer));

        assertInstanceOf(DataRepositoryException.class, thrown,
            "cache proxy must propagate the bare DataRepositoryException, not wrap it");
    }
}
