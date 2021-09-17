package uk.ac.ceh.gateway.catalogue.catalogue;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

@Slf4j
@RestController
@RequestMapping("documents")
public class CatalogueDocumentController {
    private final DocumentRepository documentRepository;

    public CatalogueDocumentController(
        DocumentRepository documentRepository
    ) {
        this.documentRepository = documentRepository;
        log.info("Creating");
    }

    @PreAuthorize("@permission.userCanView(#file)")
    @GetMapping("{file}/catalogue")
    public CatalogueResource currentCatalogue (
        @PathVariable("file") String file
    ) throws DocumentRepositoryException {
        return new CatalogueResource(documentRepository.read(file));
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @PutMapping("{file}/catalogue")
    public CatalogueResource updateCatalogue (
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file,
        @RequestBody CatalogueResource catalogueResource
    ) throws DocumentRepositoryException {
        val document = documentRepository.read(file);
        val metadata = document.getMetadata();
        document.setMetadata(
            metadata.withCatalogue(catalogueResource.getValue())
        );
        log.debug(document.toString());
        val newDocument =  documentRepository.save(
            user,
            document,
            file,
            String.format("Catalogues of %s changed.", file)
        );
        log.debug(newDocument.toString());
        return new CatalogueResource(newDocument);
    }
}
