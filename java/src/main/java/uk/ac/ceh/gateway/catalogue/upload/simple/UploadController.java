package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.nio.file.FileAlreadyExistsException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static uk.ac.ceh.gateway.catalogue.upload.simple.UploadController.Type.ERROR;
import static uk.ac.ceh.gateway.catalogue.upload.simple.UploadController.Type.INFO;

@Slf4j
@Controller
@RequestMapping("upload")
@Profile("upload:simple")
@PreAuthorize("@permission.userCanUpload(#id)")
public class UploadController {
    private final DocumentRepository documentRepository;
    private final StorageService storageService;

    public UploadController(
            DocumentRepository documentRepository,
            StorageService storageService
    ) {
        this.documentRepository = documentRepository;
        this.storageService = storageService;
        log.info("Creating {}", this);
    }

    @GetMapping(value = "{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String page(
            @PathVariable("id") String id,
            Model model,
            UriComponentsBuilder builder
    ) {
        log.info("Getting upload page for {}", id);
        model.addAttribute("id", id);
        try {
            val geminiDocument = (GeminiDocument) documentRepository.read(id);
            model.addAttribute("title", geminiDocument.getTitle());
            val files = createFileInfos(id, builder);
            model.addAttribute("files", files);
        } catch (Exception ex) {
            log.error(format("For %s error", id), ex);
            if ( !model.containsAttribute("title")) {
                model.addAttribute("title", "Unknown");
            }
            model.addAttribute("files", Collections.emptyList());
            model.addAttribute("error", "Failed to retrieve information");
        }
        log.debug("Model is {}", model);
        return "html/upload/simple/upload.ftl";
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadResponse> filenames(
            @PathVariable("id") String id,
            UriComponentsBuilder builder
    ) {
        log.info("For {} getting files", id);
        try {
            return ResponseEntity.ok(new UploadResponse(createFileInfos(id, builder)));
        } catch (Exception ex) {
            val message = format("Could not retrieve files for %s", id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new UploadResponse(message, ERROR));
        }
    }

    @PostMapping("{id}")
    public ResponseEntity<UploadResponse> upload(
            @PathVariable("id") String id,
            @RequestParam("file") MultipartFile file,
            UriComponentsBuilder builder
    ) {
        try {
            storageService.store(id, file);
            log.info("For {} uploaded {}", id, file.getOriginalFilename());
            val message = format("Successfully uploaded %s", file.getOriginalFilename());
            return ResponseEntity.ok(new UploadResponse(message, INFO, createFileInfos(id, builder)));
        } catch (FileAlreadyExistsException ex) {
            log.info("Cannot upload {} for {}, file already exists", file.getOriginalFilename(), id);
            val message = format("Could not upload %s for %s, file already exists", file.getOriginalFilename(), id);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new UploadResponse(message, ERROR));
        } catch (Exception ex) {
            log.error(format("Error uploading %s for %s", file.getOriginalFilename(), id), ex);
            val message = format("Could not upload %s for %s", file.getOriginalFilename(), id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new UploadResponse(message, ERROR));
        }
    }

    @DeleteMapping(value = "{id}/{filename:.+}")
    public ResponseEntity<UploadResponse> delete(
            @PathVariable("id") String id,
            @PathVariable("filename") String filename,
            UriComponentsBuilder builder
    ) {
        try {
            storageService.delete(id, filename);
            log.info("For {} deleting {}", id, filename);
            val message = format("Successfully deleted %s", filename);
            val files = createFileInfos(id, builder);
            return ResponseEntity.ok(new UploadResponse(message, INFO, files));
        } catch (Exception ex) {
            val message = format("Error trying to delete %s for %s", filename, id);
            log.error(message, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new UploadResponse(message, ERROR));
        }
    }

    private List<FileInfo> createFileInfos(String id, UriComponentsBuilder builder) {
        log.debug("Getting filenames for {}", id);
        return storageService.filenames(id)
                .map(filename -> new FileInfo(
                        filename,
                        builder.replacePath("upload").pathSegment(id, filename).toUriString())
                ).collect(Collectors.toList());
    }

    @Value
    public static class UploadResponse {
        String message;
        Type type;
        List<FileInfo> files;

        public UploadResponse(String message, Type type) {
            this(message, type, Collections.emptyList());
        }

        public UploadResponse(List<FileInfo> files) {
            this(null, null, files);
        }

        public UploadResponse(String message, Type type, List<FileInfo> files) {
            this.message = message;
            this.type = type;
            this.files = files;
        }
    }

    @Value
    public static class FileInfo {
        String name;
        String url;

        @SneakyThrows
        public FileInfo(String filename, String url) {
            this.name = filename;
            this.url = url;
        }
    }

    public enum Type {
        INFO, ERROR
    }
}
