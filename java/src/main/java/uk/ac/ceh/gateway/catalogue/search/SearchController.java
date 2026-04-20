package uk.ac.ceh.gateway.catalogue.search;

import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.List;

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
    public static final String SORT_FIELD_PARAM = "sortField";
    public static final String SORT_ORDER_PARAM = "order";

    public static final int PAGE_DEFAULT = Integer.parseInt(PAGE_DEFAULT_STRING);
    public static final int ROWS_DEFAULT = Integer.parseInt(ROWS_DEFAULT_STRING);

    private final Searcher searcher;

    public SearchController(
        Searcher searcher
    ) {
        this.searcher = searcher;
        log.info("Creating");
    }

    @CrossOrigin
    @SneakyThrows
    @ResponseBody
    @GetMapping("documents")
    public SearchResults searchAllCatalogues(
        @ActiveUser
        CatalogueUser user,
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
        @RequestParam(value=SORT_FIELD_PARAM, required = false)
        String sortField,
        @RequestParam(value=SORT_ORDER_PARAM, defaultValue = "asc")
        String sortOrder,
        HttpServletRequest request
    ) {
        return searcher.search(
            request.getRequestURL().toString(),
            user,
            term,
            bbox,
            SpatialOperation.valueOf(op.toUpperCase()),
            page,
            rows,
            facetFilters,
            CatalogueService.ALL_CATALOGUES_ID,
            sortField,
            "desc".equals(sortOrder) ? SolrQuery.ORDER.desc : SolrQuery.ORDER.asc
        );
    }

    @CrossOrigin
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
        @RequestParam(value=SORT_FIELD_PARAM, required = false)
        String sortField,
        @RequestParam(value=SORT_ORDER_PARAM, defaultValue = "asc")
        String sortOrder,
        HttpServletRequest request
    ) {
        return searcher.search(
            request.getRequestURL().toString(),
            user,
            term,
            bbox,
            SpatialOperation.valueOf(op.toUpperCase()),
            page,
            rows,
            facetFilters,
            catalogueKey,
            sortField,
            "desc".equals(sortOrder) ? SolrQuery.ORDER.desc : SolrQuery.ORDER.asc
        );
    }
}
