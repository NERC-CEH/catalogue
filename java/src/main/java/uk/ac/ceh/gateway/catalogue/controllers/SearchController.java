package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.search.*;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@AllArgsConstructor
public class SearchController {
    public static final String PAGE_DEFAULT_STRING = "1";
    public static final String ROWS_DEFAULT_STRING = "20";
    public static final String OP_DEFAULT_STRING = "IsWithin";
    
    public static final String TERM_QUERY_PARAM = "term";
    public static final String BBOX_QUERY_PARAM = "bbox";
    public static final String OP_QUERY_PARAM = "op";
    public static final String PAGE_QUERY_PARAM = "page";
    public static final String ROWS_QUERY_PARAM = "rows";
    public static final String FACET_QUERY_PARAM = "facet";
    
    public static final int PAGE_DEFAULT = Integer.parseInt(PAGE_DEFAULT_STRING);
    public static final int ROWS_DEFAULT = Integer.parseInt(ROWS_DEFAULT_STRING);
    
    private final SolrClient solrClient;
    private final GroupStore<CatalogueUser> groupStore;
    private final CatalogueService catalogueService;
    private final FacetFactory facetFactory;
    
    @RequestMapping (value = "documents",
                     method = RequestMethod.GET)
    public RedirectView redirectToDefaultCatalogue(
            HttpServletRequest request
    ) {
        return new RedirectView(
            ServletUriComponentsBuilder
                .fromRequest(request)
                .replacePath("{catalogue}/documents")
                .buildAndExpand(
                    catalogueService.defaultCatalogue().getId()
                )
                .toUriString()
        );
    }

    @SneakyThrows
    @GetMapping(value = "{catalogue}/documents")
    public @ResponseBody SearchResults searchDocuments(
            @ActiveUser CatalogueUser user,
            @PathVariable("catalogue") String catalogueKey,
            @RequestParam(
                value = TERM_QUERY_PARAM,
                defaultValue=SearchQuery.DEFAULT_SEARCH_TERM
            ) String term,
            @RequestParam(
                value = BBOX_QUERY_PARAM,
                required = false
            ) String bbox,
            @RequestParam(
                value = OP_QUERY_PARAM,
                defaultValue = OP_DEFAULT_STRING
            ) String op,
            @RequestParam(
                value = PAGE_QUERY_PARAM,
                defaultValue = PAGE_DEFAULT_STRING
            ) int page,
            @RequestParam(
                value = ROWS_QUERY_PARAM,
                defaultValue = ROWS_DEFAULT_STRING
            ) int rows,
            @RequestParam(
                value = FACET_QUERY_PARAM,
                defaultValue = ""
            ) List<FacetFilter> facetFilters,
            HttpServletRequest request
    ) throws SolrServerException {
        
        Catalogue catalogue = catalogueService.retrieve(catalogueKey);
        
        SearchQuery searchQuery = new SearchQuery(
            request.getRequestURL().toString(),
            user,
            term,
            bbox,
            SpatialOperation.valueOf(op.toUpperCase()),
            page,
            rows,
            facetFilters,
            groupStore,
            catalogue,
            facetFactory.newInstances(catalogue.getFacetKeys())
        );
        return new SearchResults(
            solrClient.query(
                searchQuery.build(),
                SolrRequest.METHOD.POST
            ),
            searchQuery
        ); 
    }
}