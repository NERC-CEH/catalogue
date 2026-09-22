package uk.ac.ceh.gateway.catalogue.exports;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@ToString
@Controller
public class WholeCatalogueTurtleController {

    // Turtle is only well-formed UTF-8; without an explicit charset a client can fall back to a
    // platform default and mangle the very literals dri-one #328 is about, so pin it here rather
    // than relying on the default text/turtle produced by MediaType.valueOf.
    private static final MediaType RDF_TTL_UTF8 = new MediaType(CatalogueMediaTypes.RDF_TTL, StandardCharsets.UTF_8);

    private final DocumentsToTurtleService docsToTurtle;
    private final List<String> fusekiCatalogueIds;

    public WholeCatalogueTurtleController(
        DocumentsToTurtleService docsToTurtle,
        @Value("#{'${fuseki.catalogueIds:}'.split(',')}") List<String> fusekiCatalogueIds
    ) {
        this.docsToTurtle = docsToTurtle;
        this.fusekiCatalogueIds = fusekiCatalogueIds;
    }

    @GetMapping("{catalogueId}/catalogue.ttl")
    public HttpEntity<String> getTtl(@PathVariable String catalogueId) {
        return docsToTurtle.getBigTtl(catalogueId).map(ttl -> {
            log.info("serving big turtle for {}", catalogueId);
            return ResponseEntity.ok()
                .contentType(RDF_TTL_UTF8)
                .body(ttl);
        }).orElseGet(() -> {
            log.info("not serving big turtle for unknown catalogue {}", catalogueId);
            return ResponseEntity.notFound().build();
        });
    }

    @GetMapping("catalogue.ttl")
    public HttpEntity<String> getFusekiCataloguesTtl() {
        String combined = fusekiCatalogueIds.stream()
            .filter(id -> !id.isBlank())
            .map(docsToTurtle::getBigTtl)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.joining("\n"));
        if (combined.isBlank()) {
            log.info("no Fuseki catalogues to serve");
            return ResponseEntity.notFound().build();
        }
        log.info("serving combined turtle for Fuseki catalogues: {}", fusekiCatalogueIds);
        return ResponseEntity.ok()
            .contentType(RDF_TTL_UTF8)
            .body(combined);
    }
}
