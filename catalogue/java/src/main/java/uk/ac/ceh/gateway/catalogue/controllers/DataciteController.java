package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.GEMINI_JSON_VALUE;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.ErrorResponse;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DataciteService;
import uk.ac.ceh.gateway.catalogue.services.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.services.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.services.ShortDoiService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 * The following controller will handle the generation of Datacite requests.
 * @author cjohn
 */
@Controller
public class DataciteController {
    public final static String DATACITE_ROLE = "ROLE_DATACITE";
    
    private final DataRepository<CatalogueUser> repo;
    private final BundledReaderService<MetadataDocument> documentBundleReader;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    private final DocumentWritingService<MetadataDocument> documentWriter;
    private final DataciteService dataciteService;
    private final ShortDoiService doiShortenerService;
    
    
    @Autowired
    public DataciteController(DataRepository<CatalogueUser> repo,
                              BundledReaderService<MetadataDocument> documentBundleReader,
                              DocumentInfoMapper<MetadataInfo> documentInfoMapper,
                              DocumentWritingService<MetadataDocument> documentWriter,
                              DataciteService dataciteService,
                              ShortDoiService doiShortenerService) {
        this.repo = repo;
        this.documentBundleReader = documentBundleReader;
        this.documentInfoMapper = documentInfoMapper;
        this.documentWriter = documentWriter;
        this.dataciteService = dataciteService;
        this.doiShortenerService = doiShortenerService;
    }
    
    @Secured(DATACITE_ROLE)
    @RequestMapping(value    = "documents/{file}/datacite",
                    method   = RequestMethod.POST)
    @ResponseBody
    public Object getDataciteRequest(@ActiveUser CatalogueUser user, @PathVariable("file") String file) throws DataRepositoryException, IOException, UnknownContentTypeException {
        DataRevision<CatalogueUser> latestRevision = repo.getLatestRevision();
        if(latestRevision != null) {
            MetadataDocument document = documentBundleReader.readBundle(file, latestRevision.getRevisionID());
            if(document instanceof GeminiDocument) {
                GeminiDocument geminiDocument = (GeminiDocument)document;
                if(!dataciteService.isDatacitable(geminiDocument)) {
                    return new ErrorResponse("This record does not meet the requirements for data citation");
                }
                
                MetadataInfo metadataInfo = document.getMetadata();
                metadataInfo.setRawType(GEMINI_JSON_VALUE); // The document is going to go back into the repository as JSON
                
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
                    geminiDocument.setMetadataDate(LocalDateTime.now()); // Update the last edit date
                    repo.submitData(String.format("%s.meta", file), (o)-> documentInfoMapper.writeInfo(metadataInfo, o))
                        .submitData(String.format("%s.raw", file), (o) -> documentWriter.write(geminiDocument, o))
                        .commit(user, String.format("datacite Gemini document: %s", file));
                }
                return new RedirectView(geminiDocument.getUri().toString());
            }
        }
        throw new ResourceNotFoundException("There was no gemini document present with this address");
    }
}
