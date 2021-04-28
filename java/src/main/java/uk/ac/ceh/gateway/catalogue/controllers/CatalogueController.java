package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@ToString
public class CatalogueController {
    private final DocumentRepository documentRepository;
    private final CatalogueService catalogueService;

    public CatalogueController(
        @NonNull DocumentRepository documentRepository,
        @NonNull CatalogueService catalogueService
    ) {
        this.documentRepository = documentRepository;
        this.catalogueService = catalogueService;
        log.info("Creating {}", this);
    }

    @RequestMapping(value = "catalogues", method = RequestMethod.GET)
    public  HttpEntity<List<Catalogue>> catalogues(
        @RequestParam(value = "catalogue", required = false) String catalogue,
        @RequestParam(value = "identifier", required = false) String identifier
    ) {
        List<Catalogue> catalogues = new ArrayList<>(catalogueService.retrieveAll());
        
        try {
            if(catalogue != null) {
                catalogues.remove(catalogueService.retrieve(catalogue));
            } else if (identifier != null) {
                catalogues.remove(
                    catalogueService.retrieve(
                        documentRepository.read(
                            identifier
                        ).getCatalogue()
                    )
                );
            }
        } catch (CatalogueException | DocumentRepositoryException ex) {
            // If the catalogue or identifier does not exist just return all the catalogues
        }
        return ResponseEntity.ok(catalogues);
    }

    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @RequestMapping(value = "documents/{file}/catalogue", method = RequestMethod.GET)
    public HttpEntity<CatalogueResource> currentCatalogue (
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file
    ) throws DocumentRepositoryException {
        return createCatalogueResource(
            documentRepository.read(file)
        );
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}/catalogue", method =  RequestMethod.PUT)
    public HttpEntity<CatalogueResource> updateCatalogue (
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file,
        @RequestBody CatalogueResource catalogueResource
    ) throws DocumentRepositoryException {
        MetadataDocument document = documentRepository.read(file);
        MetadataInfo metadata = document.getMetadata();
        document.setMetadata(
            metadata.withCatalogue(catalogueResource.getValue())
        );
        return createCatalogueResource(
            documentRepository.save(
                user,
                document,
                file,
                String.format("Catalogues of %s changed.", file)
            )
        );
    }

    private HttpEntity<CatalogueResource> createCatalogueResource(MetadataDocument document) {
        return ResponseEntity.ok(
            new CatalogueResource(
                document.getId(),
                document.getCatalogue()
            )
        );
    }

}
