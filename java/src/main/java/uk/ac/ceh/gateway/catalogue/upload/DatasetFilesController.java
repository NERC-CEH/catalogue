package uk.ac.ceh.gateway.catalogue.upload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.gateway.catalogue.datapreviewer.OpenDataAccess;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.HubbubResponse;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadService;

import java.io.File;
import java.util.List;

@Slf4j
@RestController
@Profile("upload-hubbub")
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DatasetFilesController {

    private final UploadService uploadService;
    private final DocumentRepository documentRepository;
    private final OpenDataAccess openDataAccess;

    @GetMapping("/{datasetId}/files")
    public ResponseEntity<?> listDatasetFiles(
        @PathVariable String datasetId,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10000") int size
    ) {
        log.debug("Fetching dataset files for {}", datasetId);

        try {
            MetadataDocument document = documentRepository.read(datasetId);
            if (document == null) {
                return notFound();
            }

            if (!(document instanceof GeminiDocument gemini) || !openDataAccess.isOpen(gemini)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Dataset not publicly accessible");
            }

            HubbubResponse hubbubResponse = uploadService.get(datasetId, "eidchub", page, size);

            if (hubbubResponse == null || hubbubResponse.getData() == null) {
                return ResponseEntity.noContent().build();
            }

            List<DatasetFile> files = hubbubResponse.getData().stream()
                .map(f -> new DatasetFile(
                    new File(f.getPath()).getName(),
                    f.getPath(),
                    f.getBytes() != null ? f.getBytes() : 0L,
                    f.getMimeType()
                ))
                .toList();

            return ResponseEntity.ok(files);
        } catch (DocumentRepositoryException e) {
            if (e.isNotFound()) {
                return notFound();
            }
            return failed(datasetId, e);
        } catch (Exception e) {
            return failed(datasetId, e);
        }
    }

    // A fixed body: echoing the id from the path would reflect caller input.
    private static ResponseEntity<String> notFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dataset not found");
    }

    // The cause stays in the log: the body must not echo exception messages to the caller.
    private static ResponseEntity<String> failed(String datasetId, Exception e) {
        log.error("Failed to fetch dataset files for {}", datasetId, e);
        return ResponseEntity.internalServerError().body("Error fetching dataset files");
    }

    public record DatasetFile(
        String name,
        String path,
        long size,
        String mimeType
    ) {}
}
