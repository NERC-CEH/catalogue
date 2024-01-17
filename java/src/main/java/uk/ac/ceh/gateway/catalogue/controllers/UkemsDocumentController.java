package uk.ac.ceh.gateway.catalogue.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.ukems.UkemsDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.*;

@Controller
public class UkemsDocumentController extends AbstractDocumentController {

    @Autowired
    public UkemsDocumentController(DocumentRepository documentRepository) {
        super(documentRepository);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = UKEMS_DOCUMENT_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newUkemsDocument(@ActiveUser CatalogueUser user, @RequestBody UkemsDocument document,
            @RequestParam("catalogue") String catalogue) throws DocumentRepositoryException {
        return saveNewMetadataDocument(user, document, catalogue, "new UK-EMS document");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = UKEMS_DOCUMENT_JSON_VALUE)
    public ResponseEntity<MetadataDocument> saveUkemsDocument(@ActiveUser CatalogueUser user, @PathVariable("file") String file,
            @RequestBody UkemsDocument document) throws DocumentRepositoryException {
        return saveMetadataDocument(user, file, document);
    }
}
