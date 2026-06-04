package uk.ac.ceh.gateway.catalogue.datacite;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.DATACITE_JSON_VALUE;

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
        log.info("Creating");
    }

    @Secured(DATACITE_ROLE)
    @GetMapping(value="{file}/datacite/getDoi", produces=DATACITE_JSON_VALUE)
    public DataciteRequest getDataciteRequestDOI(
        @PathVariable String file
    ) {
        GeminiDocument document = getDocument(file);
        log.info("getDoi endpoint hit");
        return dataciteService.getDoiMetadata(document);
    }

    @Secured(DATACITE_ROLE)
    @PostMapping(value="{file}/datacite")
    public RedirectView mintDoi(
        @ActiveUser CatalogueUser user,
        @PathVariable String file
    ) throws DocumentRepositoryException {
        GeminiDocument geminiDocument = getDocument(file);
        log.info("hit endpoint for post mintDoi");
        ResourceIdentifier doi = dataciteService.generateDoi(geminiDocument);
        geminiDocument.getResourceIdentifiers().add(doi);
        repo.save(user, geminiDocument, file, String.format("datacite Gemini document: %s", file));
        return new RedirectView(identifierService.generateUri(file));
    }

    @SneakyThrows
    private GeminiDocument getDocument(String file) {
        MetadataDocument document = repo.read(file);
        if(document instanceof GeminiDocument) {
            log.info("we have found a gemini document "+document.getId() );
            return (GeminiDocument)document;
        } else {
            throw new ResourceNotFoundException("There was no gemini document present with this address");
        }
    }

    @Secured(DATACITE_ROLE)
    @PutMapping(value="{file}/datacite/update")
    public void updateDoi(
        @ActiveUser CatalogueUser user,
        @PathVariable String file
    ) throws DocumentRepositoryException {
        GeminiDocument geminiDocument = getDocument(file);
        log.info("hit endpoint for updating Doi");
        dataciteService.updateDoiMetadata(geminiDocument);
        repo.save(user, geminiDocument, file, String.format("datacite Gemini document: %s", file));
    }
}
