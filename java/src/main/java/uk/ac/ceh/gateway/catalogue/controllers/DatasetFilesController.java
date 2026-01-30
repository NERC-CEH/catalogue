package uk.ac.ceh.gateway.catalogue.controllers;

import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.gateway.catalogue.gemini.AccessLimitation;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.HubbubResponse;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadService;

import java.io.File;
import java.util.List;

@Slf4j
@RestController
@Profile("upload:hubbub")
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DatasetFilesController {

    private final UploadService uploadService;
    private final DocumentRepository documentRepository;

    private static final String OPEN_ACCESS_URI = "http://purl.org/coar/access_right/c_abf2";

    @GetMapping("/{datasetId}/files")
    public ResponseEntity<?> listDatasetFiles(
        @PathVariable("datasetId") String datasetId,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10000") int size
    ) {
        log.info("Fetching dataset files for {}", datasetId);

        try {
            GeminiDocument document = (GeminiDocument) documentRepository.read(datasetId);
            if (document == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Dataset not found: " + datasetId);
            }

            MetadataInfo metadataInfo = document.getMetadata();
            Multimap<Permission, String> permissions = metadataInfo.getPermissions();
            String availability = document.getAvailability();
            AccessLimitation limitation = document.getAccessLimitation();

            boolean hasPublicView = permissions.containsEntry(Permission.VIEW, "public");
            boolean isAvailable = "Available".equalsIgnoreCase(availability);
            boolean isFreelyAvailable =
                limitation != null && OPEN_ACCESS_URI.equals(limitation.getUri());

            log.debug("Access check for {} -> public={}, available={}, open={}",
                datasetId, hasPublicView, isAvailable, isFreelyAvailable);

            if (!(hasPublicView && isAvailable && isFreelyAvailable)) {
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
        } catch (Exception e) {
            log.error("Failed to fetch dataset files for {}", datasetId, e);
            return ResponseEntity.internalServerError()
                .body("Error fetching dataset files: " + e.getMessage());
        }
    }

    public record DatasetFile(
        String name,
        String path,
        long size,
        String mimeType
    ) {}
}
