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
import org.springframework.context.annotation.Profile;

import uk.ac.ceh.gateway.catalogue.elter.ElterDocument;
import uk.ac.ceh.gateway.catalogue.elter.LinkedElterDocument;
import uk.ac.ceh.gateway.catalogue.elter.LinkedDocumentRetrievalService;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.ELTER_JSON_VALUE;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.LINKED_ELTER_JSON_VALUE;

@Profile("server:elter")
@Slf4j
@ToString(callSuper = true)
@Controller
public class ElterDocumentController extends AbstractDocumentController {
    private final LinkedDocumentRetrievalService linkedDocumentRetrievalService;

    public ElterDocumentController (
        DocumentRepository documentRepository,
        LinkedDocumentRetrievalService linkedDocumentRetrievalService
    ) {
        super(documentRepository);
        this.linkedDocumentRetrievalService = linkedDocumentRetrievalService;
        log.info("Creating");
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
            method = RequestMethod.POST,
            consumes = ELTER_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newElterDocument(
            @ActiveUser CatalogueUser user,
            @RequestBody ElterDocument document,
            @RequestParam("catalogue") String catalogue
    ) {
        return saveNewMetadataDocument(
                user,
                document,
                catalogue,
                "new Elter Document"
        );
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
            method = RequestMethod.PUT,
            consumes = ELTER_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateElterDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @RequestBody ElterDocument document
    ) {
        return saveMetadataDocument(
                user,
                file,
                document
        );
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
            method = RequestMethod.POST,
            consumes = LINKED_ELTER_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newLinkedElterDocument(
            @ActiveUser CatalogueUser user,
            @RequestBody LinkedElterDocument document,
            @RequestParam("catalogue") String catalogue
    ) {
        ElterDocument realDocument = linkedDocumentRetrievalService.get(document.getLinkedDocumentUri());
        realDocument.setLinkedDocument(true);
        realDocument.setLinkedDocumentUri(document.getLinkedDocumentUri());
        realDocument.setLinkedDocumentType(document.getLinkedDocumentType());
        return saveNewMetadataDocument(
                user,
                realDocument,
                catalogue,
                "new linked Elter Document"
        );
    }

}
