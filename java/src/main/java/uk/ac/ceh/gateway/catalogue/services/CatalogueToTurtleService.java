package uk.ac.ceh.gateway.catalogue.services;

import com.google.common.collect.ImmutableSet;
import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.TimeConstants;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
public class CatalogueToTurtleService implements DocumentsToTurtleService {
    private static final Set<String> REQUIRED_TYPES = ImmutableSet.of(
        "service",
        "dataset",
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
    private final ConcurrentMap<String, Optional<String>> preFetchCatalogue = new ConcurrentHashMap<>();

    public CatalogueToTurtleService(
        CatalogueService catalogueService,
        Configuration configuration,
        MetadataListingService listing,
        @Value("${documents.baseUri}") String baseUri
    ) {
        this.catalogueService = catalogueService;
        this.configuration = configuration;
        this.baseUri = baseUri;
        this.listing = listing;
    }

    @Override
    public Optional<String> getBigTtl(String catalogueId) {
        if (PREFETCH_CATALOGUES.contains(catalogueId)) {
            if (!preFetchCatalogue.containsKey(catalogueId)) {
                Optional<String> bigTtl = Optional.ofNullable(
                    catalogueService.retrieve(catalogueId)).map(catalogue -> getCatalogueTtl(catalogueId, catalogue)
                );
                preFetchCatalogue.put(catalogueId, bigTtl);
            }
            return preFetchCatalogue.get(catalogueId);
        } else {
            return Optional.ofNullable(
                catalogueService.retrieve(catalogueId)).map(catalogue -> getCatalogueTtl(catalogueId, catalogue)
            );
        }
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

    @Scheduled(initialDelay = TimeConstants.ONE_MINUTE*3, fixedDelay = TimeConstants.ONE_DAY)
    public void fetchCatalogues() {
        for (String catalogueId : PREFETCH_CATALOGUES) {
            Optional<String> bigTtl = Optional.ofNullable(
                catalogueService.retrieve(catalogueId)).map(catalogue -> getCatalogueTtl(catalogueId, catalogue)
            );
            preFetchCatalogue.put(catalogueId, bigTtl);
        }
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
        return model;
    }

    @SneakyThrows
    private String docToString(MetadataDocument model) {
        return switch (model.getType()) {
            case "dataset", "service", "aggregate" ->
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
