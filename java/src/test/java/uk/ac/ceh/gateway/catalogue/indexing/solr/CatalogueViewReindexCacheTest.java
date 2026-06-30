package uk.ac.ceh.gateway.catalogue.indexing.solr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.config.CacheConfig;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.document.reading.MetadataInfoBundledReaderService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Regression test for the stale-Solr-index bug: a record tagged into a secondary ("view-only")
 * catalogue via {@code catalogue-view} only reflected the change after a manual reindex.
 *
 * <p>Root cause: the datastore fires {@code DataSubmittedEvent} <em>synchronously</em> from inside
 * {@code commit()}, so the Solr reindex runs <em>before</em> the write path's {@code @CacheEvict} has
 * run. {@code SolrIndexingService} used to read "latest" through the mutable {@code LATEST_CACHE}
 * (and a {@code datastore-revision-id} cached at the pre-save HEAD), so it indexed the pre-save
 * document. The fix reads at the explicit, fresh revision the event already carries, via the
 * immutable {@code datastore-historical} cache.</p>
 *
 * <p>This reproduces it against the <strong>real</strong> production {@link CacheConfig} (typed
 * EHCache) and the real {@link CachedDataRepository}/{@link MetadataInfoBundledReaderService} stack —
 * mirroring {@code RevisionIdCacheKeyTypeTest}. The caches are deliberately primed with the OLD
 * record and OLD HEAD (as the controller's pre-save read does) and left un-evicted, then we index at
 * the new revision and assert the indexed document carries the NEW {@code catalogue_view}. Before the
 * fix this captured the stale (empty) value.</p>
 */
@SpringJUnitConfig(CatalogueViewReindexCacheTest.Config.class)
@DisplayName("Save-time Solr reindex reads the fresh revision, not the stale latest cache")
public class CatalogueViewReindexCacheTest {

    private static final String ID = "record-1";
    private static final String OLD_REVISION = "rev-old";
    private static final String NEW_REVISION = "rev-new";

    @EnableCaching
    @Configuration
    static class Config {
        @Bean
        CacheManager cacheManager() {
            // Real production wiring: JCacheCacheManager over String-typed EHCache.
            return new CacheConfig().cacheManager();
        }

        @Bean
        @SuppressWarnings("unchecked")
        DataRepository<CatalogueUser> dataRepository() {
            return mock(DataRepository.class);
        }

        @Bean
        CachedDataRepository cachedDataRepository(DataRepository<CatalogueUser> repo) {
            return new CachedDataRepository(repo);
        }

        @Bean
        @SuppressWarnings("unchecked")
        DocumentInfoMapper<MetadataInfo> documentInfoMapper() {
            // The bytes->MetadataInfo parse is not where the bug lives; mock it and key off a marker
            // in the cached bytes so the test stays focused on which revision's bytes the reader
            // fetches through the real cache.
            return mock(DocumentInfoMapper.class);
        }

        @Bean
        DocumentReadingService documentReadingService() {
            return mock(DocumentReadingService.class);
        }

        @Bean
        DocumentTypeLookupService documentTypeLookupService() {
            return mock(DocumentTypeLookupService.class);
        }

        @Bean
        @SuppressWarnings("unchecked")
        PostProcessingService<MetadataDocument> postProcessingService() {
            return mock(PostProcessingService.class);
        }

        @Bean
        DocumentIdentifierService documentIdentifierService() {
            return new DocumentIdentifierService("https://example.com", '-');
        }

        @Bean
        MetadataInfoBundledReaderService reader(
            CachedDataRepository cachedRepo,
            DocumentReadingService documentReader,
            DocumentInfoMapper<MetadataInfo> documentInfoMapper,
            DocumentTypeLookupService typeLookup,
            PostProcessingService<MetadataDocument> postProcessing,
            DocumentIdentifierService identifierService
        ) {
            return new MetadataInfoBundledReaderService(
                cachedRepo, documentReader, documentInfoMapper, typeLookup, postProcessing, identifierService);
        }

        @Bean
        DocumentListingService documentListingService() {
            return mock(DocumentListingService.class);
        }

        @Bean
        @SuppressWarnings("unchecked")
        IndexGenerator<MetadataDocument, SolrIndex> indexGenerator() {
            return mock(IndexGenerator.class);
        }

        @Bean
        SolrClient solrClient() {
            return mock(SolrClient.class);
        }

        @Bean
        JenaLookupService jenaLookupService() {
            return mock(JenaLookupService.class);
        }

        @Bean
        SolrIndexingService solrIndexingService(
            MetadataInfoBundledReaderService reader,
            DocumentListingService listingService,
            DataRepository<CatalogueUser> repo,
            IndexGenerator<MetadataDocument, SolrIndex> indexGenerator,
            SolrClient solrClient,
            JenaLookupService lookupService,
            DocumentIdentifierService identifierService
        ) {
            return new SolrIndexingService(
                reader, listingService, repo, indexGenerator, solrClient, lookupService, identifierService);
        }
    }

    @Autowired DataRepository<CatalogueUser> repo;
    @Autowired CachedDataRepository cachedRepo;
    @Autowired MetadataInfoBundledReaderService reader;
    @Autowired SolrIndexingService service;
    @Autowired DocumentReadingService documentReader;
    @Autowired DocumentTypeLookupService typeLookup;
    @Autowired IndexGenerator<MetadataDocument, SolrIndex> indexGenerator;
    @Autowired JenaLookupService lookupService;
    @Autowired DocumentInfoMapper<MetadataInfo> infoMapper;
    @Autowired CacheManager cacheManager;

    @BeforeEach
    public void reset() throws Exception {
        clearInvocations(repo, documentReader, typeLookup, indexGenerator, lookupService);
        for (String name : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) cache.clear();
        }
        // A fresh document per read: readBundle mutates the returned document's metadata.
        given(documentReader.read(any(), any(), any())).willAnswer(i -> new GeminiDocument());
        given(typeLookup.getType(anyString())).willAnswer(i -> GeminiDocument.class);
        given(lookupService.linked(anyString())).willReturn(Collections.emptyList());
        given(indexGenerator.generateIndex(any(MetadataDocument.class))).willReturn(new SolrIndex());
    }

    @Test
    void indexesNewCatalogueViewEvenWhenLatestCacheHoldsThePreSaveRecord() throws Exception {
        MetadataInfo base = MetadataInfo.builder()
            .rawType("application/json")
            .documentType("GEMINI_DOCUMENT")
            .catalogue("eidc")
            .build();
        MetadataInfo oldInfo = base;                                   // no secondary catalogues
        MetadataInfo newInfo = base.withCatalogueView(List.of("assist")); // tagged into "assist"

        // The .meta bytes carry a marker; readInfo (mocked) maps marker -> the right MetadataInfo.
        byte[] oldMeta = "OLD".getBytes();
        byte[] newMeta = "NEW".getBytes();
        given(infoMapper.readInfo(any())).willAnswer(i -> {
            String marker = new String(((java.io.InputStream) i.getArgument(0)).readAllBytes());
            return marker.contains("NEW") ? newInfo : oldInfo;
        });

        // The datastore as it was BEFORE the save: HEAD is rev-old, record has no catalogue-view.
        DataRevision<CatalogueUser> oldRevision = mock(DataRevision.class);
        given(oldRevision.getRevisionID()).willReturn(OLD_REVISION);
        given(repo.getLatestRevision()).willReturn(oldRevision);
        given(repo.getData(OLD_REVISION, ID + ".meta")).willAnswer(i -> blob(oldMeta));
        given(repo.getData(OLD_REVISION, ID + ".raw")).willAnswer(i -> blob("{}".getBytes()));
        // The new commit's content, addressable only at the fresh revision.
        given(repo.getData(NEW_REVISION, ID + ".meta")).willAnswer(i -> blob(newMeta));
        given(repo.getData(NEW_REVISION, ID + ".raw")).willAnswer(i -> blob("{}".getBytes()));

        // The controller reads the record before saving — this PRIMES the latest cache and the
        // cached HEAD revision with the pre-save state.
        reader.readBundle(ID);

        // The save's synchronous reindex runs with the fresh event revision while @CacheEvict has
        // not yet fired (caches still hold the pre-save state).
        service.indexDocuments(List.of(ID), NEW_REVISION);

        ArgumentCaptor<MetadataDocument> indexed = ArgumentCaptor.forClass(MetadataDocument.class);
        verify(indexGenerator).generateIndex(indexed.capture());
        assertThat(indexed.getValue().getMetadata().getCatalogueView())
            .as("the indexed document must reflect the catalogue-view from the just-saved revision")
            .containsExactly("assist");
    }

    private static DataDocument blob(byte[] content) {
        DataDocument doc = mock(DataDocument.class);
        try {
            given(doc.getInputStream()).willAnswer(i -> new ByteArrayInputStream(content));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return doc;
    }
}
