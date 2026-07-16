package uk.ac.ceh.gateway.catalogue.modelnerc;

import lombok.ToString;
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

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.*;

@Slf4j
@ToString(callSuper = true)
@Controller
@RequestMapping("documents")
public class NercModelController extends AbstractDocumentController {

    public NercModelController(DocumentRepository documentRepository, CachedDataRepository cachedDataRepository) {
        super(documentRepository, cachedDataRepository);
        log.info("Creating");
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @PostMapping(consumes = NERC_MODEL_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newNercModel(
            @ActiveUser CatalogueUser user,
            @RequestBody NercModel document,
            @RequestParam("catalogue") String catalogue
    ) {
        return saveNewMetadataDocument(user, document, catalogue, "new Model");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @PutMapping(value = "{file}", consumes = NERC_MODEL_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateNercModel(
            @ActiveUser CatalogueUser user,
            @PathVariable String file,
            @RequestBody NercModel document,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch
    ) {
        return saveMetadataDocument(user, file, document, ifMatch);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @PostMapping(consumes = NERC_MODEL_USE_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newNercModelUse(
            @ActiveUser CatalogueUser user,
            @RequestBody NercModelUse document,
            @RequestParam("catalogue") String catalogue
    ) {
        return saveNewMetadataDocument(user, document, catalogue, "new Model implementation (NERC)");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @PutMapping(value = "{file}", consumes = NERC_MODEL_USE_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateNercModelUse(
            @ActiveUser CatalogueUser user,
            @PathVariable String file,
            @RequestBody NercModelUse document,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch
    ) {
        return saveMetadataDocument(user, file, document, ifMatch);
    }


}
