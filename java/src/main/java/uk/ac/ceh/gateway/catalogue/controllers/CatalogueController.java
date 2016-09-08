package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
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
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueResource;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

@Slf4j
@Controller
@RequestMapping(value = "documents/{file}/catalogue")
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
    
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<CatalogueResource> currentCatalogue (
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file
    ) throws IOException, DataRepositoryException, UnknownContentTypeException, PostProcessingException {
        return createCatalogueResource(
            documentRepository.read(file)
        ); 
    }
    
    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(method =  RequestMethod.PUT)
    @ResponseBody
    public HttpEntity<CatalogueResource> updateCatalogue (
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file,
        @RequestBody CatalogueResource catalogueResource
    ) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        MetadataDocument document = documentRepository.read(file);
        document
            .getMetadata()
            .setCatalogue(
                catalogueResource.getValue()
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
