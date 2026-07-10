package uk.ac.ceh.gateway.catalogue.services;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lists metadata documents from the data repository that are publicly accessible, in three
 * variants that are NOT interchangeable — each was shaped for its actual callers, not for a
 * generic "list documents" need:
 *
 * <ul>
 *   <li>{@link #getPublicDocuments} — cached, type/resourceType-filtered, {@code eidc}-only IDs.
 *   Used by the WAF controllers.</li>
 *   <li>{@link #getPublicDocumentsOfCatalogue} — uncached, any catalogue (including secondary
 *   {@code catalogue-view} membership), returns IDs only, pinned to one revision for the whole
 *   scan. Used where only the ID is needed (sitemap, quality checks, counts).</li>
 *   <li>{@link #getLatestPublicDocumentsOfCatalogue} — uncached, same catalogue-membership rules,
 *   but returns full {@link MetadataDocument} objects. Added by EMC-581 specifically for
 *   {@code CatalogueToTurtleService}, which previously called {@link #getPublicDocumentsOfCatalogue}
 *   for IDs and then re-read each document by ID to render it — an N+1 double-read that caused the
 *   {@code catalogue.ttl} export to time out. Do not "simplify" this back into a single method: an
 *   ID-only caller reading full documents just to discard them, or the Turtle export reintroducing
 *   its double read, are both regressions this split exists to prevent.</li>
 * </ul>
 */
@Slf4j
@ToString
@Service
public class MetadataListingService {
    public static final String METADATA_LISTINGS_CACHE = "metadata-listings";
    private static final String WAF_CATALOGUE = "eidc";
    private final DataRepository<CatalogueUser> repo;
    private final DocumentListingService listingService;
    private final BundledReaderService<MetadataDocument> documentBundleReader;

    public MetadataListingService(
        DataRepository<CatalogueUser> repo,
        DocumentListingService listingService,
        BundledReaderService<MetadataDocument> documentBundleReader
    ) {
        this.repo = repo;
        this.listingService = listingService;
        this.documentBundleReader = documentBundleReader;
        log.info("Creating");
    }

    /**
     * Returns a list of metadata ids of documents which are:
     * - publicly accessible,
     * - of the correct document type (e.g. GeminiDocument),
     * - have a resourceType of dataset, series or service,
     * - belong to the {@code eidc} catalogue specifically (unlike the {@code *OfCatalogue}
     * methods below, which accept any catalogue and also honour secondary
     * {@code catalogue-view} membership).
     *
     * <p>Cached under {@link #METADATA_LISTINGS_CACHE} (3-minute expire-after-access). There is no
     * explicit eviction: {@code revision} is part of the default cache key, and every save/delete
     * advances the repository's HEAD, so callers that always pass the current revision (as both
     * existing callers do) naturally get a fresh cache entry after a write. A previous revision's
     * entry is simply abandoned, not evicted — it ages out on its own once nothing requests that
     * revision again.</p>
     *
     * @param revision      of the data repository to read from
     * @param type          of the document to list (eg GeminiDocument)
     * @param resourceTypes resourceTypes describe a bit more about a metadata
     *                      document than its simple type, e.g. GeminiMetadata documents can be
     *                      Datasets, Series, Service, etc. So this param lists the resourceTypes
     *                      we want document ids for.
     * @return a list of metadata ids
     * @throws DataRepositoryException if the repository cannot be read
     * @throws IOException             if a document's bundle cannot be read
     */
    @Cacheable(METADATA_LISTINGS_CACHE)
    public List<String> getPublicDocuments(String revision, Class<? extends MetadataDocument> type, List<String> resourceTypes) throws DataRepositoryException, IOException, PostProcessingException {
        List<String> toReturn = new ArrayList<>();
        List<String> documents = listingService.filterFilenames(repo.getFiles(revision));

        log.debug("Building metadata listing @ {} of type: {}", revision, type);
        for (String file : documents) {
            try {
                MetadataDocument doc = documentBundleReader.readBundle(file, revision);
                if (
                    type.isAssignableFrom(doc.getClass()) &&
                        doc.getMetadata().isPubliclyViewable(Permission.VIEW) &&
                        caseInsensitiveContains(resourceTypes, doc.getType()) &&
                        doc.getCatalogue().equals(WAF_CATALOGUE)
                ) {
                    toReturn.add(doc.getId());
                }
            } catch (RuntimeException ex) {
                log.error("Failed to read " + file + " @ " + revision);
            }
        }
        return toReturn;
    }

    /**
     * Returns the ids of publicly viewable documents belonging to {@code catalogue} — either as
     * their primary catalogue, or via secondary {@code catalogue-view} membership (see
     * {@link #isInCatalogue}). Not cached itself, but reads each document via
     * {@code readBundle(file, currentRevision)}, which goes through
     * {@link uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository#readAtRevision} — the
     * immutable, revision-pinned {@code datastore-historical} cache — so the whole scan is a
     * consistent snapshot at one revision.
     *
     * <p>Use this when only the id is needed (sitemap generation, quality checks, counts). If full
     * document objects are needed instead, use {@link #getLatestPublicDocumentsOfCatalogue}, not
     * this method followed by a second per-id read — that double-read pattern is exactly what
     * caused the {@code catalogue.ttl} export timeout (EMC-581) that method was added to fix.</p>
     */
    @SneakyThrows
    public List<String> getPublicDocumentsOfCatalogue(String catalogue) {
        List<String> toReturn = Lists.newArrayList();
        String currentRevision = repo.getLatestRevision().getRevisionID();
        List<String> documents = listingService.filterFilenames(repo.getFiles(currentRevision));

        for (String file : documents) {
            try {
                MetadataDocument doc = documentBundleReader.readBundle(file, currentRevision);
                if (
                    doc.getMetadata().isPubliclyViewable(Permission.VIEW) &&
                        isInCatalogue(doc, catalogue)
                ) {
                    toReturn.add(doc.getId());
                }
            } catch (RuntimeException ex) {
                log.error("Failed to read " + file + " @ " + currentRevision);
            }
        }
        return toReturn;
    }

    /**
     * Returns the full {@link MetadataDocument}s (not just ids) for publicly viewable documents
     * belonging to {@code catalogue}, using the same catalogue-membership rules as
     * {@link #getPublicDocumentsOfCatalogue}. Added by EMC-581 so {@code CatalogueToTurtleService}
     * can render each document's Turtle in a single pass, instead of its previous approach —
     * {@link #getPublicDocumentsOfCatalogue} for ids, then a second per-id read to get the document
     * — which double-read every record and caused the {@code catalogue.ttl} export to time out.
     *
     * <p>Not cached itself. Unlike {@link #getPublicDocumentsOfCatalogue}, document reads go through
     * the no-revision {@code readBundle(file)} overload, which re-resolves HEAD via the cached
     * {@link uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository#getLatestRevisionId} for
     * every document rather than pinning to the {@code currentRevision} used to list files — in
     * practice both resolve to the same HEAD, but under a concurrent write mid-scan they could
     * diverge by one revision. This is deliberate (it always wants "latest", not a pinned
     * snapshot); it is not a bug to "fix" by pinning it to match
     * {@link #getPublicDocumentsOfCatalogue}.</p>
     */
    @SneakyThrows
    public List<MetadataDocument> getLatestPublicDocumentsOfCatalogue(String catalogue) {
        List<MetadataDocument> toReturn = Lists.newArrayList();
        String currentRevision = repo.getLatestRevision().getRevisionID();
        List<String> documents = listingService.filterFilenames(repo.getFiles(currentRevision));
        for (String file : documents) {
            try {
                MetadataDocument doc = documentBundleReader.readBundle(file);
                if (doc.getMetadata().isPubliclyViewable(Permission.VIEW) &&
                    isInCatalogue(doc, catalogue)) {
                    toReturn.add(doc);
                }
            } catch (RuntimeException ex) {
                log.error("Failed to read " + file + " @ " + currentRevision);
            }
        }
        return toReturn;
    }

    private boolean isInCatalogue(MetadataDocument doc, String catalogue) {
        if (doc.getCatalogue().equalsIgnoreCase(catalogue)) return true;
        return doc.getMetadata().getCatalogueView().stream()
            .anyMatch(c -> c.equalsIgnoreCase(catalogue));
    }

    private boolean caseInsensitiveContains(List<String> referenceList, String testValue) {
        return referenceList
            .stream()
            .anyMatch((s) -> s.equalsIgnoreCase(testValue));
    }
}
