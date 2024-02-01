package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.infrastructure.InfrastructureRecord;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.INFRASTRUCTURERECORD_JSON_VALUE;

@Slf4j
@Controller
@RequestMapping("documents")
public class InfrastructureRecordController extends AbstractDocumentController {

    public InfrastructureRecordController(DocumentRepository documentRepository) {
        super(documentRepository);
        log.info("Creating");
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @PostMapping(consumes = INFRASTRUCTURERECORD_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newInfrastructureRecord(
            @ActiveUser CatalogueUser user,
            @RequestBody InfrastructureRecord document,
            @RequestParam("catalogue") String catalogue
    ) {
        return saveNewMetadataDocument(user, document, catalogue, "new infrastructure record");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @PutMapping(value = "{file}", consumes = INFRASTRUCTURERECORD_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateInfrastructureRecord(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @RequestBody InfrastructureRecord document
    ) {
        return saveMetadataDocument(user, file, document);
    }
}
