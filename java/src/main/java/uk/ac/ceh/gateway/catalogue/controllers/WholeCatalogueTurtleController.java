package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.ac.ceh.gateway.catalogue.services.DocumentsToTurtleService;

@Slf4j
@ToString
@Controller
public class WholeCatalogueTurtleController {

    private final DocumentsToTurtleService docsToTurtle;

    public WholeCatalogueTurtleController(DocumentsToTurtleService docsToTurtle) {
        this.docsToTurtle = docsToTurtle;
    }

    @GetMapping("{catalogueId}/catalogue.ttl")
    @SneakyThrows
    public HttpEntity<String> getTtl(@PathVariable String catalogueId) {
        return docsToTurtle.getBigTtl(catalogueId).map(ttl -> {
            log.info("serving big turtle for {}", catalogueId);
            return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/turtle"))
                .body(ttl);
        }).orElseGet(() -> {
            log.info("not serving big turtle for unknown catalogue {}", catalogueId);
            return ResponseEntity.notFound().build();
        });
    }
}
