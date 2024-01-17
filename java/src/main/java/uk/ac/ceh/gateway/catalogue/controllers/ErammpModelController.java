package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpModel;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.ERAMMP_MODEL_JSON_VALUE;

@Slf4j
@ToString(callSuper = true)
@Controller
@RequestMapping("documents")
public class ErammpModelController extends AbstractDocumentController {

    public ErammpModelController(DocumentRepository documentRepository) {
        super(documentRepository);
        log.info("Creating {}", this);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @PostMapping(consumes = ERAMMP_MODEL_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newErammpModel(
            @ActiveUser CatalogueUser user,
            @RequestBody ErammpModel document,
            @RequestParam("catalogue") String catalogue
    ) {
        return saveNewMetadataDocument(user, document, catalogue, "new ERAMMP model");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @PutMapping(value = "{file}", consumes = ERAMMP_MODEL_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateErammpModel(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @RequestBody ErammpModel document
    ) {
        return saveMetadataDocument(user, file, document);
    }
}
