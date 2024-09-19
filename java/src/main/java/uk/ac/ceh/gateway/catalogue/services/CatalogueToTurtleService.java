package uk.ac.ceh.gateway.catalogue.services;

import com.google.common.collect.ImmutableSet;
import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.util.*;
import java.util.stream.Collectors;

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
    private final CatalogueService catalogueService;
    private final Configuration configuration;
    private final DocumentRepository documentRepository;
    private final MetadataListingService listing;
    private final String baseUri;

    public CatalogueToTurtleService(
        CatalogueService catalogueService,
        DocumentRepository documentRepository,
        Configuration configuration,
        MetadataListingService listing,
        @Value("${documents.baseUri}") String baseUri
    ) {
        this.catalogueService = catalogueService;
        this.configuration = configuration;
        this.documentRepository = documentRepository;
        this.baseUri = baseUri;
        this.listing = listing;
    }

    @Override
    public Optional<String> getBigTtl(String catalogueId) {
        return Optional.ofNullable(catalogueService.retrieve(catalogueId)).map(catalogue -> {
            List<String> ids = getRequiredIds(catalogueId);
            String catalogueTtl = generateCatalogueTtl(getCatalogueModel(catalogue, ids));
            List<String> recordsTtl = getRecordsTtl(ids);

            String bigTtl = catalogueTtl.concat(String.join("\n", recordsTtl));
            log.debug("Big turtle to send: {}", bigTtl);
            return bigTtl;
        });
    }

    private List<String> getRequiredIds(String catalogueId) {
        try {
            List<String> ids = listing.getPublicDocumentsOfCatalogue(catalogueId);

            return ids.stream()
                .map(this::getMetadataDocument)
                .filter(doc -> REQUIRED_TYPES.contains(doc.getType()))
                .map(MetadataDocument::getId)
                .collect(Collectors.toList());
        } catch (NullPointerException e) {
            // no git commits
            return Collections.emptyList();
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

    private List<String> getRecordsTtl(List<String> ids) {
        return ids.stream()
            .map(this::getMetadataDocument)
            .map(this::docToString)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

    @SneakyThrows
    private MetadataDocument getMetadataDocument(String id) {
        return documentRepository.read(id);
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
