package uk.ac.ceh.gateway.catalogue.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.gateway.catalogue.model.SearchResults;

@Controller
public class SearchController {
    
    @RequestMapping(value = "documents",
                    method = RequestMethod.GET)
    public @ResponseBody SearchResults searchDocuments(
            @RequestParam(value = "term", required = false) String term,
            @RequestParam(value = "start", defaultValue = "0") long start,
            @RequestParam(value = "rows", defaultValue = "20") long rows
    ){
        return performQuery(term, start, rows);
    }
    
    private SearchResults performQuery(String term, long start, long rows){
        SearchResults toReturn = null;
        return toReturn;
    }
    
}