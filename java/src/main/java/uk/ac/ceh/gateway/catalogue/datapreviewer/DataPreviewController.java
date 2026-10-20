package uk.ac.ceh.gateway.catalogue.datapreviewer;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves a record's data preview as JSON: the manifest, a CSV sample and a converted supporting
 * document. Failures carry only a generic body; the cause is logged and never sent to the browser.
 */
@Slf4j
@RestController
@Profile("upload-hubbub")
@ConditionalOnProperty(name = "catalogue.data-preview.enabled", havingValue = "true")
@RequestMapping("/documents/{id}/data-preview")
@RequiredArgsConstructor
public class DataPreviewController {

    private final DataPreviewService service;

    @GetMapping("")
    public PreviewManifest manifest(@PathVariable String id) {
        return service.manifest(id);
    }

    @GetMapping("/csv")
    public CsvSample csv(@PathVariable String id, @RequestParam String path) {
        return orNotFound(service.csv(id, path));
    }

    @GetMapping("/document")
    public DocumentPreview document(@PathVariable String id, @RequestParam String path) {
        return orNotFound(service.document(id, path));
    }

    private static <T> T orNotFound(Optional<T> result) {
        return result.orElseThrow(() -> new NoSuchElementException("Path is not in the listing"));
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Map<String, String>> notFound(NoSuchElementException e) {
        log.debug("Data preview target not found", e);
        return respond(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PreviewNotOpenException.class)
    ResponseEntity<Map<String, String>> notOpen(PreviewNotOpenException e) {
        log.debug("Data preview refused", e);
        return respond(HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(PreviewBusyException.class)
    ResponseEntity<Map<String, String>> busy(PreviewBusyException e) {
        log.info("Data preview busy: {}", e.getMessage());
        return respond(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<Map<String, String>> failed(RuntimeException e) {
        log.error("Data preview failed", e);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static ResponseEntity<Map<String, String>> respond(HttpStatus status) {
        return ResponseEntity.status(status).body(Map.of("error", "Preview unavailable"));
    }
}
