package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.GEMINI_XML_SHORT;

/**
 * The following emulates a Web accessible Folder of gemini metadata records 
 * from the current catalogue
 */
@Slf4j
@ToString
@Controller
@RequestMapping("documents/gemini/waf")
public class GeminiWafController {
    private final DataRepository<CatalogueUser> repo;
    private final MetadataListingService listing;

    public GeminiWafController( DataRepository<CatalogueUser> repo,
                                MetadataListingService listing) {
        this.repo = repo;
        this.listing = listing;
        log.info("Creating {}", this);
    }
    
    @RequestMapping(value="/",
                    method=RequestMethod.GET)
    public ModelAndView getWaf() throws DataRepositoryException, IOException, PostProcessingException {
        List<String> resourceTypes = Arrays.asList("dataset", "service");
        DataRevision<CatalogueUser> latestRevision = repo.getLatestRevision();
        List<String> files = (latestRevision == null) ? Collections.emptyList() : listing
                .getPublicDocuments(latestRevision.getRevisionID(), GeminiDocument.class, resourceTypes)
                .stream()
                .map((d)-> d + ".xml")
                .collect(Collectors.toList());
        return new ModelAndView("/html/waf.ftl", "files", files);
    }
    
    @RequestMapping("{id}.xml")
    public String forwardToMetadata(@PathVariable("id") String id) {
        return "forward:/documents/" + id + "?format=" + GEMINI_XML_SHORT;
    }
}
