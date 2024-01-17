package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.sa.SampleArchive;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.SAMPLE_ARCHIVE_JSON_VALUE;

@Slf4j
@ToString(callSuper = true)
@Controller
public class SampleArchiveController extends AbstractDocumentController {

    public SampleArchiveController(DocumentRepository documentRepository) {
        super(documentRepository);
        log.info("Creating {}", this);
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = SAMPLE_ARCHIVE_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newSampleArchive(@ActiveUser CatalogueUser user, @RequestBody SampleArchive document,
            @RequestParam("catalogue") String catalogue) {
        return saveNewMetadataDocument(user, document, catalogue, "new Sample Archive");
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}", method = RequestMethod.PUT, consumes = SAMPLE_ARCHIVE_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateSampleArchive(@ActiveUser CatalogueUser user, @PathVariable("file") String file,
            @RequestBody SampleArchive document) {
        return saveMetadataDocument(user, file, document);
    }
}
