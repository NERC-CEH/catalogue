package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.util.Collections;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@Controller
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

    @GetMapping("upload/{id}")
    public String getUploadPage(
            @PathVariable("id") String id,
            Model model
    ) {
        log.info("Getting upload page for {}", id);
        model.addAttribute("id", id);
        try {
            val geminiDocument = (GeminiDocument) documentRepository.read(id);
            model.addAttribute("title", geminiDocument.getTitle());
            val files = storageService.filenames(id)
                    .map(filename -> new FileInfo(id, filename))
                    .collect(Collectors.toList());
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

    @PostMapping("documents/{id}/add-upload-document")
    public String handleFileUpload(
            @PathVariable("id") String id,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {
        log.info("For {} uploaded {}", id, file.getOriginalFilename());
        redirectAttributes.addAttribute("id", id);
        try {
            storageService.store(id, file);
            redirectAttributes.addFlashAttribute(
                    "message",
                    format("Successfully uploaded %s", file.getOriginalFilename())
            );
        } catch (FileAlreadyExistsException ex) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    format("Cannot upload %s, file already exists", file.getOriginalFilename())
            );
        }
        return "redirect:/upload/{id}";
    }

    @DeleteMapping("documents/{id}/delete-upload-file/{filename:.+}")
    public String handleFileDeletion(
            @PathVariable("id") String id,
            @PathVariable("filename") String filename,
            RedirectAttributes redirectAttributes
    ) {
        log.info("For {} deleting {}", id, filename);
        storageService.delete(id, filename);
        redirectAttributes
                .addAttribute("id", id)
                .addFlashAttribute("message", format("Successfully deleted %s", filename));
        return "redirect:/upload/{id}";
    }

    @Value
    public static class FileInfo {
        String name;
        String deleteUrl;

        @SneakyThrows
        public FileInfo(String id, String filename) {
            this.name = filename;
            val encoded = UriUtils.encodePath(filename, StandardCharsets.UTF_8.name());
            this.deleteUrl = format("/documents/%s/delete-upload-file/%s", id, encoded);
        }
    }
}
