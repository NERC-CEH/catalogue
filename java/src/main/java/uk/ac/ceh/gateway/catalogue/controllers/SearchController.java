package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.search.*;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

@Slf4j
@ToString
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

    private final SolrClient solrClient;
    private final GroupStore<CatalogueUser> groupStore;
    private final CatalogueService catalogueService;
    private final FacetFactory facetFactory;

    public SearchController(
        SolrClient solrClient,
        GroupStore<CatalogueUser> groupStore,
        CatalogueService catalogueService,
        FacetFactory facetFactory
    ) {
        this.solrClient = solrClient;
        this.groupStore = groupStore;
        this.catalogueService = catalogueService;
        this.facetFactory = facetFactory;
        log.info("Creating");
    }

    @GetMapping("documents")
    public RedirectView redirectToDefaultCatalogue(
            HttpServletRequest request
    ) {
        val defaultCatalogueId = catalogueService.defaultCatalogue().getId();
        val redirectUrl = ServletUriComponentsBuilder
            .fromRequest(request)
            .replacePath("{catalogue}/documents")
            .buildAndExpand(defaultCatalogueId)
            .toUriString();
        log.info("Redirecting to {}", redirectUrl);
        return new RedirectView(redirectUrl);
    }

    @SneakyThrows
    @ResponseBody
    @GetMapping("{catalogue}/documents")
    public SearchResults search(
        @ActiveUser
        CatalogueUser user,
        @PathVariable("catalogue")
        String catalogueKey,
        @RequestParam(value=TERM_QUERY_PARAM, defaultValue=SearchQuery.DEFAULT_SEARCH_TERM)
        String term,
        @RequestParam(value=BBOX_QUERY_PARAM, required = false)
        String bbox,
        @RequestParam(value=OP_QUERY_PARAM, defaultValue=OP_DEFAULT_STRING)
        String op,
        @RequestParam(value=PAGE_QUERY_PARAM, defaultValue=PAGE_DEFAULT_STRING)
        int page,
        @RequestParam(value=ROWS_QUERY_PARAM, defaultValue=ROWS_DEFAULT_STRING)
        int rows,
        @RequestParam(value=FACET_QUERY_PARAM, defaultValue = "")
        List<FacetFilter> facetFilters,
        HttpServletRequest request
    ) {

        val catalogue = catalogueService.retrieve(catalogueKey);

        val searchQuery = new SearchQuery(
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
        val response = solrClient.query(
            "documents",
            searchQuery.build(),
            POST
        );
        return new SearchResults(response, searchQuery);
    }
}
