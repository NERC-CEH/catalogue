package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpDatacube;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.ERAMMP_DATACUBE_JSON_VALUE;

@Slf4j
@Controller
@RequestMapping("documents")
public class ErammpDatacubeController extends AbstractDocumentController {

    public ErammpDatacubeController(DocumentRepository documentRepository) {
        super(documentRepository);
        log.info("Creating");
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @PostMapping(consumes = ERAMMP_DATACUBE_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newErammpDatacube(
            @ActiveUser CatalogueUser user,
            @RequestBody ErammpDatacube document,
            @RequestParam("catalogue") String catalogue
    ) {
        return saveNewMetadataDocument(user, document, catalogue, "new ERAMMP data cube");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @PutMapping(value = "{file}", consumes = ERAMMP_DATACUBE_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateErammpDatacube(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @RequestBody ErammpDatacube document
    ) {
        return saveMetadataDocument(user, file, document);
    }
}
