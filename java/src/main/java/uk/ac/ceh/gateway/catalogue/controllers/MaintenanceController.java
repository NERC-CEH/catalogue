package uk.ac.ceh.gateway.catalogue.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;

/**
 *
 * @author Christopher Johnson
 */
@Controller
@RequestMapping("maintenance")
public class MaintenanceController {
    
    private final DocumentIndexingService solrIndex;
    
    @Autowired
    public MaintenanceController( DocumentIndexingService solrIndex ) {
        this.solrIndex = solrIndex;
    }
    
    @RequestMapping(value="/documents/reindex",
                    method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void reindexDocuments() throws DocumentIndexingException {
        solrIndex.rebuildIndex();
    }
}