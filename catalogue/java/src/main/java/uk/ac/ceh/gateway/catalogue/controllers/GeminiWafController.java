package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import static uk.ac.ceh.gateway.catalogue.config.WebConfig.GEMINI_XML_SHORT;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;

/**
 * The following emulates a Web accessible Folder of gemini metadata records 
 * from the current catalogue
 * @author cjohn
 */
@Controller
@RequestMapping("documents/gemini/waf")
public class GeminiWafController {
    private final DataRepository<CatalogueUser> repo;
    private final MetadataListingService listing;
    
    @Autowired
    public GeminiWafController( DataRepository<CatalogueUser> repo,
                                MetadataListingService listing) {
        this.repo = repo;
        this.listing = listing;
    }
    
    @RequestMapping(value="/",
                    method=RequestMethod.GET)
    public ModelAndView getWaf() throws DataRepositoryException, IOException, PostProcessingException {
        List<String> resourceTypes = Arrays.asList("dataset", "service");
        DataRevision<CatalogueUser> latestRevision = repo.getLatestRevision();
        List<String> files = (latestRevision == null) ? Collections.EMPTY_LIST : listing
                .getPublicDocuments(latestRevision.getRevisionID(), GeminiDocument.class, resourceTypes)
                .stream()
                .map((d)-> d + ".xml")
                .collect(Collectors.toList());
        return new ModelAndView("/html/waf.html.tpl", "files", files);
    }
    
    @RequestMapping("{id}.xml")
    public String forwardToMetadata(@PathVariable("id") String id) {
        return "forward:/documents/" + id + "?format=" + GEMINI_XML_SHORT;
    }
}
