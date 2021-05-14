package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.ToString;
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
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import java.util.Collections;
import java.util.List;

import static java.lang.String.format;

@Slf4j
@ToString
@Controller
@RequestMapping("upload")
@Profile("upload:simple")
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
    @PreAuthorize("@permission.userCanUpload(#id)")
    @SneakyThrows(DocumentRepositoryException.class)
    public String page (
            @PathVariable("id") String id,
            Model model
    ) {
        try {
            log.info("Getting upload page for {}", id);
            model.addAttribute("id", id);
            val geminiDocument = (GeminiDocument) documentRepository.read(id);
            model.addAttribute("title", geminiDocument.getTitle());
            model.addAttribute("catalogueKey", geminiDocument.getMetadata().getCatalogue());
            val files = storageService.filenames(id);
            model.addAttribute("files", files);
        } catch (StorageServiceException ex) {
            log.error(format("For %s error", id), ex);
            if ( !model.containsAttribute("title")) {
                model.addAttribute("title", "Error - Unknown");
            }
            model.addAttribute("files", Collections.emptyList());
            model.addAttribute("message", new ErrorMessage(ex.getMessage()));
        }
        log.debug("Model is {}", model);
        return "html/upload/simple/upload";
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@permission.userCanUpload(#id)")
    public ResponseEntity<List<FileInfo>> filenames (
            @PathVariable("id") String id,
            UriComponentsBuilder builder
    ) {
        log.info("For {} getting filenames", id);
        return ResponseEntity.ok(storageService.filenames(id));
    }

    @PostMapping("{id}")
    @PreAuthorize("@permission.userCanUpload(#id)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upload(
            @PathVariable("id") String id,
            @RequestParam("file") MultipartFile file
    ) {
        storageService.store(id, file);
        log.info("Successfully uploaded {} for {}", file.getOriginalFilename(), id);
    }

    @DeleteMapping(value = "{id}/{filename:.+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permission.userCanUpload(#id)")
    public void delete(
            @PathVariable("id") String id,
            @PathVariable("filename") String filename
    ) {
        storageService.delete(id, filename);
        log.info("Successfully deleted {} for {}", filename, id);
    }

    @ExceptionHandler
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleFileNotFound(UserInputException ex) {
        return handleException(ex);
    }

    @ExceptionHandler
    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage handleFileAlreadyExists(FileExitsException ex) {
        return handleException(ex);
    }

    @ExceptionHandler
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleNoSuchFile(StorageServiceException ex) {
        return handleException(ex);
    }

    private ErrorMessage handleException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return new ErrorMessage(ex.getMessage());
    }

    @Value
    public static class ErrorMessage {
        String message;
        String type = "error";
    }
}
