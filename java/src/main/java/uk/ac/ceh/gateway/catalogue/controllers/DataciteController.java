package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteResponse;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.DATACITE_XML_VALUE;

/**
 * The following controller will handle the generation of Datacite requests.
 */
@Slf4j
@ToString
@RestController
@RequestMapping("documents")
public class DataciteController {
    public final static String DATACITE_ROLE = "ROLE_DATACITE";

    private final DocumentRepository repo;
    private final DocumentIdentifierService identifierService;
    private final DataciteService dataciteService;

    public DataciteController(
        DocumentRepository repo,
        DocumentIdentifierService identifierService,
        DataciteService dataciteService
    ) {
        this.repo = repo;
        this.identifierService = identifierService;
        this.dataciteService = dataciteService;
        log.info("Creating {}", this);
    }

    @GetMapping(value="{file}/datacite.xml")
    public DataciteResponse getDataciteRequestXml(
        @PathVariable("file") String file
    ) {
        return getDataciteRequest(file);
    }

    @GetMapping(value="{file}/datacite", produces=DATACITE_XML_VALUE)
    public DataciteResponse getDataciteRequest(
        @PathVariable("file") String file
    ) {
        return dataciteService.getDataciteResponse(getDocument(file));
    }

    @Secured(DATACITE_ROLE)
    @PostMapping(value="{file}/datacite")
    public RedirectView mintDoi(
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file
    ) throws DocumentRepositoryException {
        GeminiDocument geminiDocument = getDocument(file);

        ResourceIdentifier doi = dataciteService.generateDoi(geminiDocument);
        geminiDocument.getResourceIdentifiers().add(doi);
        repo.save(user, geminiDocument, file, String.format("datacite Gemini document: %s", file));
        return new RedirectView(identifierService.generateUri(file));
    }

    @SneakyThrows
    private GeminiDocument getDocument(String file) {
        MetadataDocument document = repo.read(file);
        if(document instanceof GeminiDocument) {
            return (GeminiDocument)document;
        } else {
            throw new ResourceNotFoundException("There was no gemini document present with this address");
        }
    }
}
