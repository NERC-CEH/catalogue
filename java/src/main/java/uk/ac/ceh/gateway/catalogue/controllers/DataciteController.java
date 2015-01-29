package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.DATACITE_XML_VALUE;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

/**
 * The following controller will handle the generation of Datacite requests.
 * @author cjohn
 */
@Controller
public class DataciteController {
    private final DataRepository<CatalogueUser> repo;
    private final BundledReaderService<MetadataDocument> documentBundleReader;
    
    @Autowired
    public DataciteController(DataRepository<CatalogueUser> repo,
                              BundledReaderService<MetadataDocument> documentBundleReader) {
        this.repo = repo;
        this.documentBundleReader = documentBundleReader;
    }
    
    @RequestMapping(value    = "documents/{file}/datacite.xml",
                    method   = RequestMethod.GET,
                    produces = DATACITE_XML_VALUE)
    public ModelAndView getDataciteRequest(HttpServletResponse response, @PathVariable("file") String file) throws DataRepositoryException, IOException, UnknownContentTypeException {
        String revision = repo.getLatestRevision().getRevisionID();
        MetadataDocument document = documentBundleReader.readBundle(file, revision);
        if(document instanceof GeminiDocument) {
            response.setContentType(DATACITE_XML_VALUE);
            Map<String, Object> data = new HashMap<>();
            data.put("doc", document);
            data.put("resourceType", getDataciteResourceType((GeminiDocument)document));
            return new ModelAndView("/datacite/datacite.xml.tpl", data);
        }
        else {
            throw new ResourceNotFoundException("This document is not a gemini document, so does not have online resources");
        }
    }
    
    private String getDataciteResourceType(GeminiDocument document) {
        switch(document.getResourceType()) {
            case "nonGeographicDataset": return "Dataset";
            case "dataset":              return "Dataset";
            case "application":          return "Application";
            case "model":                return "Model";
            case "service":              return "Service";
            case "software":             return "Software";
            default:                     return null;
        }
    }
}
