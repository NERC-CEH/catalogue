package uk.ac.ceh.gateway.catalogue.search;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.List;
import java.util.Optional;

@Slf4j
@ToString
@Controller
@Tag(name = "Search", description = "Full-text and faceted search across environmental metadata records")
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
    public static final String SEMANTIC_QUERY_PARAM = "semantic";

    public static final int PAGE_DEFAULT = Integer.parseInt(PAGE_DEFAULT_STRING);
    public static final int ROWS_DEFAULT = Integer.parseInt(ROWS_DEFAULT_STRING);

    private final Searcher searcher;
    private final Optional<SemanticSearcher> semanticSearcher;
    private final GroupStore<CatalogueUser> groupStore;
    private final String semanticGroup;

    public SearchController(
        Searcher searcher,
        Optional<SemanticSearcher> semanticSearcher,
        GroupStore<CatalogueUser> groupStore,
        @Value("${catalogue.semantic.group:}") String semanticGroup
    ) {
        this.searcher = searcher;
        this.semanticSearcher = semanticSearcher;
        this.groupStore = groupStore;
        this.semanticGroup = semanticGroup;
        log.info("Creating — semantic group restriction: '{}'",
            semanticGroup.isBlank() ? "none" : semanticGroup);
    }

    private boolean userCanUseSemantic(CatalogueUser user) {
        if (semanticGroup.isBlank()) return true;
        return groupStore.getGroups(user).stream()
            .map(Group::getName)
            .anyMatch(semanticGroup::equalsIgnoreCase);
    }

    @Operation(
        summary = "Search all catalogues",
        description = "Returns paginated metadata records matching the given term across all catalogues.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Search results"),
            @ApiResponse(responseCode = "400", description = "Invalid parameter (e.g. unrecognised spatial operator)")
        }
    )
    @CrossOrigin
    @SneakyThrows
    @ResponseBody
    @GetMapping("documents")
    public SearchResults searchAllCatalogues(
        @ActiveUser
        CatalogueUser user,
        @Parameter(description = "Free-text search term. Use * to return all records.", example = "nitrogen deposition")
        @RequestParam(value=TERM_QUERY_PARAM, defaultValue=SearchQuery.DEFAULT_SEARCH_TERM)
        String term,
        @Parameter(description = "Bounding box filter in WGS84 decimal degrees: minLon,maxLon,maxLat,minLat.", example = "-3.5,1.8,53.0,50.0")
        @RequestParam(value=BBOX_QUERY_PARAM, required = false)
        String bbox,
        @Parameter(description = "Spatial relationship between the result geometry and the bounding box.",
            schema = @Schema(allowableValues = {"IsWithin", "Intersects"}, defaultValue = "IsWithin"))
        @RequestParam(value=OP_QUERY_PARAM, defaultValue=OP_DEFAULT_STRING)
        String op,
        @Parameter(description = "Page number (1-based).")
        @RequestParam(value=PAGE_QUERY_PARAM, defaultValue=PAGE_DEFAULT_STRING)
        int page,
        @Parameter(description = "Number of records per page.")
        @RequestParam(value=ROWS_QUERY_PARAM, defaultValue=ROWS_DEFAULT_STRING)
        int rows,
        @Parameter(description = "Facet filters in `field|value` format, URL-encoded. Repeatable. Example: `topic%7CHydrology`.")
        @RequestParam(value=FACET_QUERY_PARAM, defaultValue = "")
        List<FacetFilter> facetFilters,
        @Parameter(description = "Field to sort by. Omit for relevance ranking.")
        @RequestParam(value=SORT_FIELD_PARAM, required = false)
        String sortField,
        @Parameter(description = "Sort direction.",
            schema = @Schema(allowableValues = {"asc", "desc"}, defaultValue = "asc"))
        @RequestParam(value=SORT_ORDER_PARAM, defaultValue = "asc")
        String sortOrder,
        @Parameter(description = "Use semantic (KNN vector) search powered by Amazon Bedrock instead of BM25 full-text search. " +
            "Requires the `vector-search` Spring profile and AWS Bedrock credentials. " +
            "Access may be restricted to a specific Crowd group via `catalogue.semantic.group`. " +
            "Falls back to BM25 silently if embeddings are not configured or the user lacks access.")
        @RequestParam(value=SEMANTIC_QUERY_PARAM, defaultValue = "false")
        boolean semantic,
        HttpServletRequest request
    ) {
        val endpoint = request.getRequestURL().toString();
        val spatialOp = SpatialOperation.valueOf(op.toUpperCase());
        val canUseSemantic = semanticSearcher.isPresent() && userCanUseSemantic(user);
        if (semantic && canUseSemantic) {
            return new SearchResults(semanticSearcher.get().search(endpoint, user, term, bbox, spatialOp, page, rows, CatalogueService.ALL_CATALOGUES_ID), true);
        }
        return new SearchResults(searcher.search(endpoint, user, term, bbox, spatialOp, page, rows, facetFilters, CatalogueService.ALL_CATALOGUES_ID, sortField,
            "desc".equals(sortOrder) ? SolrQuery.ORDER.desc : SolrQuery.ORDER.asc), canUseSemantic);
    }

    @Operation(
        summary = "Search within a catalogue",
        description = "Returns paginated metadata records matching the given term, scoped to a single catalogue.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Search results"),
            @ApiResponse(responseCode = "400", description = "Invalid parameter (e.g. unrecognised spatial operator)")
        }
    )
    @CrossOrigin
    @SneakyThrows
    @ResponseBody
    @GetMapping("{catalogue}/documents")
    public SearchResults search(
        @ActiveUser
        CatalogueUser user,
        @Parameter(description = "Catalogue identifier, e.g. `eidc`.", example = "eidc")
        @PathVariable("catalogue")
        String catalogueKey,
        @Parameter(description = "Free-text search term. Use * to return all records.", example = "nitrogen deposition")
        @RequestParam(value=TERM_QUERY_PARAM, defaultValue=SearchQuery.DEFAULT_SEARCH_TERM)
        String term,
        @Parameter(description = "Bounding box filter in WGS84 decimal degrees: minLon,maxLon,maxLat,minLat.", example = "-3.5,1.8,53.0,50.0")
        @RequestParam(value=BBOX_QUERY_PARAM, required = false)
        String bbox,
        @Parameter(description = "Spatial relationship between the result geometry and the bounding box.",
            schema = @Schema(allowableValues = {"IsWithin", "Intersects"}, defaultValue = "IsWithin"))
        @RequestParam(value=OP_QUERY_PARAM, defaultValue=OP_DEFAULT_STRING)
        String op,
        @Parameter(description = "Page number (1-based).")
        @RequestParam(value=PAGE_QUERY_PARAM, defaultValue=PAGE_DEFAULT_STRING)
        int page,
        @Parameter(description = "Number of records per page.")
        @RequestParam(value=ROWS_QUERY_PARAM, defaultValue=ROWS_DEFAULT_STRING)
        int rows,
        @Parameter(description = "Facet filters in `field|value` format, URL-encoded. Repeatable. Example: `topic%7CHydrology`.")
        @RequestParam(value=FACET_QUERY_PARAM, defaultValue = "")
        List<FacetFilter> facetFilters,
        @Parameter(description = "Field to sort by. Omit for relevance ranking.")
        @RequestParam(value=SORT_FIELD_PARAM, required = false)
        String sortField,
        @Parameter(description = "Sort direction.",
            schema = @Schema(allowableValues = {"asc", "desc"}, defaultValue = "asc"))
        @RequestParam(value=SORT_ORDER_PARAM, defaultValue = "asc")
        String sortOrder,
        @Parameter(description = "Use semantic (KNN vector) search powered by Amazon Bedrock instead of BM25 full-text search. " +
            "Requires the `vector-search` Spring profile and AWS Bedrock credentials. " +
            "Access may be restricted to a specific Crowd group via `catalogue.semantic.group`. " +
            "Falls back to BM25 silently if embeddings are not configured or the user lacks access.")
        @RequestParam(value=SEMANTIC_QUERY_PARAM, defaultValue = "false")
        boolean semantic,
        HttpServletRequest request
    ) {
        val endpoint = request.getRequestURL().toString();
        val spatialOp = SpatialOperation.valueOf(op.toUpperCase());
        val canUseSemantic = semanticSearcher.isPresent() && userCanUseSemantic(user);
        if (semantic && canUseSemantic) {
            return new SearchResults(semanticSearcher.get().search(endpoint, user, term, bbox, spatialOp, page, rows, catalogueKey), true);
        }
        return new SearchResults(searcher.search(endpoint, user, term, bbox, spatialOp, page, rows, facetFilters, catalogueKey, sortField,
            "desc".equals(sortOrder) ? SolrQuery.ORDER.desc : SolrQuery.ORDER.asc), canUseSemantic);
    }
}
