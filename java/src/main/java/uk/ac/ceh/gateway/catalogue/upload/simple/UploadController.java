package uk.ac.ceh.gateway.catalogue.upload.simple;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadDocument;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@Profile("upload:simple")
public class UploadController {
    private final DocumentRepository documentRepository;

    public UploadController(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
        log.info("Creating {}", this);
    }

    @SneakyThrows
    @PreAuthorize("@permission.userCanUpload(#id)")
    @GetMapping("upload/{id}")
    public ModelAndView getUploadPage(
            @PathVariable("id") String id
    ) {
        log.info("Getting upload page for {}", id);
        val geminiDocument = (GeminiDocument) documentRepository.read(id);
        Map<String, Object> model = new HashMap<>();
        model.put("id", id);
        model.put("title", geminiDocument.getTitle());
        log.debug("Model is {}", model);
        return new ModelAndView("/html/upload/simple/upload-document.ftl", model);
    }

    @PreAuthorize("@permission.userCanUpload(#id)")
    @PostMapping("documents/{id}/add-upload-document")
    public ResponseEntity<UploadDocument> addFile(
            @PathVariable("id") String id,
            @RequestParam("file") MultipartFile file
    ) {
        log.info("For {} uploaded {}", id, file.getOriginalFilename());
        return ResponseEntity.noContent().build();
    }
}
