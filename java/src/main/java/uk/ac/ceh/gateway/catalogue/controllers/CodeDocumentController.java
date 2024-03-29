package uk.ac.ceh.gateway.catalogue.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CodeDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.CODE_JSON_VALUE;

@Controller
@RequestMapping("documents")
public class CodeDocumentController extends AbstractDocumentController {

    @Autowired
    public CodeDocumentController(DocumentRepository documentRepository) {
        super(documentRepository);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @PostMapping(consumes = CODE_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newCodeDocument(
            @ActiveUser CatalogueUser user,
            @RequestBody CodeDocument document,
            @RequestParam("catalogue") String catalogue
    ) {
        return saveNewMetadataDocument(user, document, catalogue, "new code document");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @PutMapping(value = "{file}", consumes = CODE_JSON_VALUE)
    public ResponseEntity<MetadataDocument> saveCodeDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @RequestBody CodeDocument document
    ) {
        return saveMetadataDocument(user, file, document);
    }
}
