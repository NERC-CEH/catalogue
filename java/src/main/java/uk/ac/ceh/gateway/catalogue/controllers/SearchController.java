package uk.ac.ceh.gateway.catalogue.controllers;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.NonNull;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.search.FacetFactory;
import uk.ac.ceh.gateway.catalogue.search.FacetFilter;
import uk.ac.ceh.gateway.catalogue.search.SearchResults;
import uk.ac.ceh.gateway.catalogue.search.SearchQuery;
import uk.ac.ceh.gateway.catalogue.search.SpatialOperation;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;

@Controller
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
    
    private final SolrServer solrServer;
    private final GroupStore<CatalogueUser> groupStore;
    private final CatalogueService catalogueService;
    private final FacetFactory facetFactory;
      
    @Autowired
    public SearchController(
        @NonNull SolrServer solrServer,
        @NonNull GroupStore<CatalogueUser> groupStore,
        @NonNull CatalogueService catalogueService,
        @NonNull FacetFactory facetFactory
    )
    {
        this.solrServer = solrServer;
        this.groupStore = groupStore;
        this.catalogueService = catalogueService;
        this.facetFactory = facetFactory;
    }
    
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
    
    @RequestMapping(value = "{catalogue}/documents",
                    method = RequestMethod.GET)
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
                defaultValue = OP_DEFAULT_STRING) String op,
            @RequestParam(
                value = PAGE_QUERY_PARAM,
                defaultValue = PAGE_DEFAULT_STRING) int page,
            @RequestParam(
                value = ROWS_QUERY_PARAM,
                defaultValue = ROWS_DEFAULT_STRING) int rows,
            @RequestParam(
                value = FACET_QUERY_PARAM,
                defaultValue = "") List<FacetFilter> facetFilters,
            HttpServletRequest request
    ) throws SolrServerException {
        String endpoint = ServletUriComponentsBuilder
                                            .fromRequest(request)
                                            .replacePath("documents")
                                            .toUriString();
        
        Catalogue catalogue = catalogueService.retrieve(catalogueKey);
        
        SearchQuery searchQuery = new SearchQuery(
            endpoint,
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
            solrServer.query(
                searchQuery.build(),
                SolrRequest.METHOD.POST
            ),
            searchQuery
        ); 
    }
}