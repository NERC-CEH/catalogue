package uk.ac.ceh.gateway.catalogue.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.controllers.AbstractDocumentController;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.INFRASTRUCTURERECORD_JSON_VALUE;

import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("documents")
public class InfrastructureRecordController extends AbstractDocumentController {

    public InfrastructureRecordController(DocumentRepository documentRepository, CachedDataRepository cachedDataRepository) {
        super(documentRepository, cachedDataRepository);
        log.info("Creating");
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @PostMapping(consumes = INFRASTRUCTURERECORD_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newInfrastructureRecord(
            @ActiveUser CatalogueUser user,
            @RequestBody InfrastructureRecord document,
            @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException, IOException {
        return saveNewMetadataDocument(user, document, catalogue, "new infrastructure record");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @PutMapping(value = "{file}", consumes = INFRASTRUCTURERECORD_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateInfrastructureRecord(
            @ActiveUser CatalogueUser user,
            @PathVariable String file,
            @RequestBody InfrastructureRecord document,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch
    ) throws DocumentRepositoryException, IOException {
        return saveMetadataDocument(user, file, document, ifMatch);
    }
}
