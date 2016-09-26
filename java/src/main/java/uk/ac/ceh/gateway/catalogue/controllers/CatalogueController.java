package uk.ac.ceh.gateway.catalogue.controllers;

import java.util.List;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.CatalogueResource;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;

@Controller
public class CatalogueController {
    private final DocumentRepository documentRepository;
    private final CatalogueService catalogueService;

    @Autowired
    public CatalogueController(
        @NonNull DocumentRepository documentRepository,
        @NonNull CatalogueService catalogueService
    ) {
        this.documentRepository = documentRepository;
        this.catalogueService = catalogueService;
    }
    
    @RequestMapping(value = "catalogues", method = RequestMethod.GET)
    @ResponseBody
    public  HttpEntity<List<Catalogue>> catalogues() {
        return ResponseEntity.ok(catalogueService.retrieveAll());
    }   
    
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @RequestMapping(value = "documents/{file}/catalogue", method = RequestMethod.GET)
    @ResponseBody
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
    @ResponseBody
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
