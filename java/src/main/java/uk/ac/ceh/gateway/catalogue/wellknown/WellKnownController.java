package uk.ac.ceh.gateway.catalogue.wellknown;

import freemarker.template.Configuration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Profile("exports")
@Slf4j
@ToString
@RestController
@Tag(name = "Discovery", description = "Machine-readable service description endpoints")
public class WellKnownController {

    private final Configuration freemarkerConfig;
    private final String baseUri;
    private final String sparqlUrl;
    private final List<String> catalogueIds;
    private final CatalogueService catalogueService;
    private final VoidStatsService voidStatsService;

    public WellKnownController(
        Configuration freemarkerConfig,
        @Value("${documents.baseUri}") String baseUri,
        @Value("${fuseki.sparqlEndpoint}") String sparqlUrl,
        @Value("#{'${fuseki.catalogueIds:}'.split(',')}") List<String> catalogueIds,
        CatalogueService catalogueService,
        VoidStatsService voidStatsService
    ) {
        this.freemarkerConfig = freemarkerConfig;
        this.baseUri = baseUri;
        this.sparqlUrl = sparqlUrl;
        this.catalogueIds = catalogueIds.stream().filter(id -> !id.isBlank()).toList();
        this.catalogueService = catalogueService;
        this.voidStatsService = voidStatsService;
        log.info("Creating");
    }

    @Operation(
        summary = "VoID dataset description",
        description = "W3C VoID description of the catalogue datasets, declaring the SPARQL endpoint and data dumps for automated discovery"
    )
    @GetMapping(value = ".well-known/void", produces = "text/turtle")
    public ResponseEntity<String> getVoidDescription() throws Exception {
        List<Catalogue> catalogues = catalogueIds.stream()
            .map(catalogueService::retrieve)
            .toList();
        Map<String, VoidStats> stats = catalogueIds.stream()
            .flatMap(id -> voidStatsService.get(id).map(s -> Map.entry(id, s)).stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, Object> model = new HashMap<>();
        model.put("baseUri", baseUri);
        model.put("sparqlUrl", sparqlUrl);
        model.put("catalogues", catalogues);
        model.put("stats", stats);
        String body = FreeMarkerTemplateUtils.processTemplateIntoString(
            freemarkerConfig.getTemplate("rdf/void.ftl"),
            model
        );
        return ResponseEntity.ok()
            .contentType(MediaType.valueOf("text/turtle"))
            .body(body);
    }
}
