package uk.ac.ceh.gateway.catalogue.exports;

import com.google.common.collect.ImmutableSet;
import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.TimeConstants;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
public class CatalogueToTurtleService implements DocumentsToTurtleService {
    private static final Set<String> REQUIRED_TYPES = ImmutableSet.of(
        "service",
        "dataset",
        "nonGeographicDataset",
        "aggregate",
        "monitoringActivity",
        "monitoringFacility",
        "monitoringNetwork",
        "monitoringProgramme"
    );
    private static final Set<String> PREFETCH_CATALOGUES = ImmutableSet.of(
        "eidc"
    );
    private final CatalogueService catalogueService;
    private final Configuration configuration;
    private final MetadataListingService listing;
    private final String baseUri;
    private final DocumentIndexingService linkIndex;
    private final ConcurrentMap<String, Optional<String>> preFetchCatalogue = new ConcurrentHashMap<>();

    public CatalogueToTurtleService(
        CatalogueService catalogueService,
        Configuration configuration,
        MetadataListingService listing,
        @Value("${documents.baseUri}") String baseUri,
        @Qualifier("jena-index") DocumentIndexingService linkIndex
    ) {
        this.catalogueService = catalogueService;
        this.linkIndex = linkIndex;
        this.configuration = configuration;
        this.baseUri = baseUri;
        this.listing = listing;
    }

    @Override
    public Optional<String> getBigTtl(String catalogueId) {
        if (PREFETCH_CATALOGUES.contains(catalogueId)) {
            return getCachedBigTtl(catalogueId);
        } else {
            return getUncachedBigTtl(catalogueId);
        }
    }

    private Optional<String> getCachedBigTtl(String catalogueId) {
        Optional<String> cached = preFetchCatalogue.get(catalogueId);
        return cached != null ? cached : fetch(catalogueId);
    }

    /**
     * Renders a catalogue's Turtle and caches it - unless the Jena index was empty or being rebuilt
     * at any point while it was rendered. Record-to-record links are read from that index, which is
     * unpersisted and so rebuilt for the first minutes after every start; caching then would serve
     * the catalogue without them for up to a day (dri-one #408). This is the only place the cache is
     * written, so it covers the scheduled prefetch, {@link #refresh()} and a request's cache miss
     * alike. Turtle that isn't cached is still returned, and an earlier, complete entry is kept.
     */
    private Optional<String> fetch(String catalogueId) {
        boolean readyBefore = linkIndex.isReady();
        Optional<String> ttl = getUncachedBigTtl(catalogueId);
        if (readyBefore && linkIndex.isReady()) {
            preFetchCatalogue.put(catalogueId, ttl);
        } else {
            log.info("Not caching the {} catalogue Turtle: the Jena index is empty or being rebuilt", catalogueId);
        }
        return ttl;
    }

    private Optional<String> getUncachedBigTtl(String catalogueId) {
        return Optional.ofNullable(
            catalogueService.retrieve(catalogueId)).map(catalogue -> getCatalogueTtl(catalogueId, catalogue)
        );
    }

    private String getCatalogueTtl(String catalogueId, Catalogue catalogue) {
        List<String> ids = new ArrayList<>();
        List<String> recordsTtls = new ArrayList<>();
        try {
            List<MetadataDocument> publicDocs = listing.getLatestPublicDocumentsOfCatalogue(catalogueId);
            for (MetadataDocument doc : publicDocs) {
                if (REQUIRED_TYPES.contains(doc.getType())) {
                    ids.add(doc.getId());
                    String ttl = docToString(doc);
                    if (!ttl.isBlank()) {
                        recordsTtls.add(ttl);
                    }
                }
            }
        } catch (NullPointerException e) {
            ids.clear();
            recordsTtls.clear();
        }

        String catalogueTtl = generateCatalogueTtl(getCatalogueModel(catalogue, ids));
        return catalogueTtl.concat(String.join("\n", recordsTtls));
    }

    /**
     * The daily prefetch. Skipped while the Jena index isn't ready, since what it rendered couldn't
     * be cached (see {@link #fetch(String)}); the first request once the index is ready fills the
     * cache instead.
     */
    @Scheduled(initialDelay = TimeConstants.ONE_MINUTE*3, fixedDelay = TimeConstants.ONE_DAY)
    public void fetchCatalogues() {
        if (!linkIndex.isReady()) {
            log.info("Not prefetching catalogue Turtle: the Jena index is empty or being rebuilt");
            return;
        }
        for (String catalogueId : PREFETCH_CATALOGUES) {
            fetch(catalogueId);
        }
    }

    /**
     * Forces the prefetched catalogues (e.g. eidc) to be rebuilt from the current documents, rather than
     * served from whatever {@link #fetchCatalogues()} last put in the cache - up to a day old. Called by
     * a manual export trigger so it publishes current data rather than the stale prefetch.
     */
    @Override
    public void refresh() {
        fetchCatalogues();
    }

    @SneakyThrows
    private String generateCatalogueTtl(Map<String, Object> model) {
        val freemarkerTemplate = configuration.getTemplate("rdf/catalogue.ttl.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, model);
    }

    private Map<String, Object> getCatalogueModel(Catalogue catalogue, List<String> ids) {
        Map<String, Object> model = new HashMap<>();
        model.put("records", ids);
        model.put("catalogue", catalogue.getId());
        model.put("title", catalogue.getTitle());
        model.put("baseUri", baseUri);
        model.put("exported", Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        return model;
    }

    @SneakyThrows
    private String docToString(MetadataDocument model) {
        return switch (model.getType()) {
            case "dataset", "nonGeographicDataset", "service", "aggregate" ->
                template(model, "rdf/ttlUnprefixed.ftl");
            case "monitoringActivity" ->
                template(model, "rdf/monitoring/unprefixed/activity.ftl");
            case "monitoringFacility" ->
                template(model, "rdf/monitoring/unprefixed/facility.ftl");
            case "monitoringNetwork" ->
                template(model, "rdf/monitoring/unprefixed/network.ftl");
            case "monitoringProgramme" ->
                template(model, "rdf/monitoring/unprefixed/programme.ftl");
            default -> "";
        };
    }

    @SneakyThrows
    private String template(MetadataDocument model, String templateName) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate(templateName),
            model
        );
    }
}
