package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.nio.file.Path;
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

    @SneakyThrows
    @GetMapping("upload/{id}")
    public String getUploadPage(
            @PathVariable("id") String id,
            Model model
    ) {
        log.info("Getting upload page for {}", id);
        val geminiDocument = (GeminiDocument) documentRepository.read(id);
        val filenames = storageService.loadAll(id)
                .map(Path::getFileName)
                .collect(Collectors.toList());
        model.addAttribute("id", id);
        model.addAttribute("title", geminiDocument.getTitle());
        model.addAttribute("files", filenames);
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
        storageService.store(id, file);
        redirectAttributes
                .addAttribute("id", id)
                .addFlashAttribute("message", format("Successfully uploaded %s", file.getOriginalFilename()));
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
}
