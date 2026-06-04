package uk.ac.ceh.gateway.catalogue.citation;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

@Slf4j
@ToString
@RestController
public class CitationController {
    private final DocumentRepository documentRepository;
    private final CitationService citationService;

    public CitationController(DocumentRepository documentRepository, CitationService citationService) {
        this.documentRepository = documentRepository;
        this.citationService = citationService;
        log.info("Creating");
    }

    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @GetMapping("documents/{file}/citation")
    public ResponseEntity<Citation> getCitationByFormat(
        @ActiveUser CatalogueUser user,
        @PathVariable String file,
        @RequestParam(value = "format", required = false) String format
    ) throws DocumentRepositoryException {
        Citation citation = getCitation(documentRepository.read(file));
        HttpHeaders headers = new HttpHeaders();
        if (format != null) {
            headers.setContentDispositionFormData("attachment", file + "-citation." + format);
        }

        return ResponseEntity.ok()
            .headers(headers)
            .body(citation);
    }

    @PreAuthorize("@permission.toAccess(#user, #file, #revision, 'VIEW')")
    @GetMapping("history/{revision}/{file}/citation")
    public Citation getCitationHistory(
        @ActiveUser CatalogueUser user,
        @PathVariable String file,
        @PathVariable String revision
    ) throws DocumentRepositoryException  {
        return getCitation(documentRepository.read(file, revision));
    }

    private Citation getCitation(MetadataDocument document) {
        log.debug("Citation for: {}", document.getId());
        if(document instanceof GeminiDocument gemini) {
            return citationService.getCitation(gemini)
                .orElseThrow(
                    () -> new ResourceNotFoundException("The Document is not citable")
                );
        }
        else {
            throw new ResourceNotFoundException("Only Gemini Documents can have citations");
        }
    }
}
