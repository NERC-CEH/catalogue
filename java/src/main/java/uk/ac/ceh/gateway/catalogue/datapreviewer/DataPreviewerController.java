package uk.ac.ceh.gateway.catalogue.datapreviewer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Profile("upload:hubbub")
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DataPreviewerController {

    private final DataPreviewerService dataPreviewerService;

    @GetMapping("/{id}/preview")
    public ResponseEntity<?> preview(@PathVariable String id) {
        try {
            return ResponseEntity.ok(dataPreviewerService.preview(id));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to generate preview for {}", id, e);
            return ResponseEntity.internalServerError()
                .body("Failed to generate preview");
        }
    }
}
