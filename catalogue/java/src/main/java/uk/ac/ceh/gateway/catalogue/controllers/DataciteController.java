package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.DATACITE_XML_VALUE;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.ErrorResponse;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.services.DataciteService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.ShortDoiService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 * The following controller will handle the generation of Datacite requests.
 * @author cjohn
 */
@Controller
public class DataciteController {
    public final static String DATACITE_ROLE = "ROLE_DATACITE";
    
    private final DocumentRepository repo;
    private final DocumentIdentifierService identifierService;
    private final DataciteService dataciteService;
    private final ShortDoiService doiShortenerService;
    
    
    @Autowired
    public DataciteController(DocumentRepository repo,
                              DocumentIdentifierService identifierService,
                              DataciteService dataciteService,
                              ShortDoiService doiShortenerService) {
        this.repo = repo;
        this.identifierService = identifierService;
        this.dataciteService = dataciteService;
        this.doiShortenerService = doiShortenerService;
    }
        
    @RequestMapping(value    = "documents/{file}/datacite.xml",
                    method   = RequestMethod.GET,
                    produces = DATACITE_XML_VALUE)
    @ResponseBody
    public String getDataciteRequest(@PathVariable("file") String file) throws IOException, DataRepositoryException, UnknownContentTypeException, PostProcessingException {
        return dataciteService.getDatacitationRequest(getDocument(file));
    }
    
    @Secured(DATACITE_ROLE)
    @RequestMapping(value    = "documents/{file}/datacite",
                    method   = RequestMethod.POST)
    @ResponseBody
    public Object mintDoi(@ActiveUser CatalogueUser user, @PathVariable("file") String file) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        GeminiDocument geminiDocument = getDocument(file);

        ResourceIdentifier doi = dataciteService.generateDoi(geminiDocument);
        geminiDocument.getResourceIdentifiers().add(doi);
        try {
            //By this point we have been successful in generating a DOI for the record.
            //If possible, we want to generate a shortDoi and attach this to the record
            //as well. If that fails, still save the metadata record
            ResourceIdentifier shortDoi = doiShortenerService.shortenDoi(doi.getCode());
            geminiDocument.getResourceIdentifiers().add(shortDoi);
        }
        catch(RestClientException ex) {
            return new ErrorResponse("The record has had a DOI added, but we failed to add a short doi: " + ex.getMessage());
        }
        finally {
            repo.save(user, geminiDocument, file);
        }
        return new RedirectView(identifierService.generateUri(file));
    }
    
    protected GeminiDocument getDocument(String file) throws DataRepositoryException, IOException, UnknownContentTypeException, PostProcessingException {
        MetadataDocument document = repo.read(file);
        if(document instanceof GeminiDocument) {
            return (GeminiDocument)document;
        } else {
            throw new ResourceNotFoundException("There was no gemini document present with this address");
        }
    }
}
