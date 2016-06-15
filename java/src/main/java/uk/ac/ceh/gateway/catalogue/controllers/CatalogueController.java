package uk.ac.ceh.gateway.catalogue.controllers;

import java.net.URI;
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
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueResource;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.MetadataInfoEditingService;

@Controller
@RequestMapping(value = "documents/{file}/catalogue")
public class CatalogueController {
    private final MetadataInfoEditingService metadataInfoEditingService;
    private final DocumentIdentifierService documentIdentifierService;

    @Autowired
    public CatalogueController(
        @NonNull MetadataInfoEditingService metadataInfoEditingService,
        @NonNull DocumentIdentifierService documentIdentifierService
    ) {
        this.metadataInfoEditingService = metadataInfoEditingService;
        this.documentIdentifierService = documentIdentifierService;
    }
    
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<CatalogueResource> currentCatalogue (
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file
    ) {
        URI metadataUri = URI.create(
            documentIdentifierService.generateUri(file)
        );
        MetadataDocument document = metadataInfoEditingService
            .getMetadataDocument(file, metadataUri);
        return ResponseEntity.ok(new CatalogueResource(document)); 
    }
    
    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(method =  RequestMethod.PUT)
    @ResponseBody
    public HttpEntity<CatalogueResource> updateCatalogue (
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file,
        @RequestBody CatalogueResource catalogueResource
    ) throws DataRepositoryException {
        URI metadataUri =  URI.create(
            documentIdentifierService.generateUri(file)
        );
        MetadataInfo info = metadataInfoEditingService
            .getMetadataDocument(file, metadataUri)
            .getMetadata();
        catalogueResource.updateCatalogues(info);
        String commitMsg = String.format("Catalogues of %s changed.", file);
        metadataInfoEditingService.saveMetadataInfo(
            file,
            info,
            user,
            commitMsg
        );
        return ResponseEntity.ok(
            new CatalogueResource(
                metadataInfoEditingService.getMetadataDocument(
                    file,
                    metadataUri
                )
            )
        ); 
    }

}
