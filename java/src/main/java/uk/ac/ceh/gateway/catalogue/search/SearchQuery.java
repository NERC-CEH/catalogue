package uk.ac.ceh.gateway.catalogue.search;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

import javax.validation.constraints.NotNull;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static uk.ac.ceh.gateway.catalogue.search.SearchController.*;

@Value
@Slf4j
public class SearchQuery {
    public static final String DEFAULT_SEARCH_TERM = "*";
    private static final String RANDOM_DYNAMIC_FIELD_NAME = "random";

    String endpoint;
    CatalogueUser user;
    @NotNull String term;
    String bbox;
    SpatialOperation spatialOperation;
    int page;
    int rows;
    @NotNull List<FacetFilter> facetFilters;
    GroupStore<CatalogueUser> groupStore;
    Catalogue catalogue;
    List<Facet> facets;

    public SearchQuery(
            String endpoint,
            CatalogueUser user,
            String term,
            String bbox,
            SpatialOperation spatialOperation,
            int page,
            int rows,
            List<FacetFilter> facetFilters,
            GroupStore<CatalogueUser> groupStore,
            Catalogue catalogue,
            List<Facet> facets
    ) {
        this.endpoint = endpoint;
        this.user = user;
        this.term = term;
        this.bbox = bbox;
        this.spatialOperation = spatialOperation;
        this.page = page;
        this.rows = rows;
        this.facetFilters = new ArrayList<>(facetFilters);
        this.groupStore = groupStore;
        this.facets = facets;
        this.catalogue = catalogue;
    }

    public SolrQuery build(){
        SolrQuery query = new SolrQuery()
                .setQuery(term)
                .setParam("defType", "edismax")
                .setParam("qf", "title^50 description^25 keyword^2 lineage organisation individual surname altTitle resourceIdentifier identifier supplementalDescription supplementalName infrastructureCapabilities^2")
                .setParam("bq", "resourceStatus:Available^100, resourceStatus:Controlled^100, resourceStatus:Embargoed^80, resourceStatus:Restricted^80, resourceStatus:Superseded^10, resourceStatus:Deleted^1")
                .setParam("bf", "version")
                .setParam("ps", "5")
                .setParam("pf", "title^50 description^25 keyword^2 supplementalDescription")
                .setStart((page-1)*rows)
                .setRows(rows);
        setSpatialFilter(query);
        setRecordVisibility(query);
        setFacetFilters(query);
        setFacetFields(query);
        setSortOrder(query);
        setCatalogueFilter(query);
        log.debug("search query: {}", query);
        return query;
    }

    public String getTermNotDefault() {
        return (DEFAULT_SEARCH_TERM.equals(term))? "" : term;
    }

    public SearchQuery withPage(int newPage) {
        if ( page != newPage) {
            return new SearchQuery(
                endpoint,
                user,
                term,
                bbox,
                spatialOperation,
                newPage,
                rows,
                facetFilters,
                groupStore,
                catalogue,
                facets
            );
        }
        else {
            return this;
        }
    }

    /**
     * Generate a search query with a new bbox value. This will fundamentally
     * change the search query so we will jump back to page one.
     * @param newBbox the new bbox value or null to remove bbox filtering
     * @return A new search query if the new bbox value would result in a search change
     */
    public SearchQuery withBbox(String newBbox) {
        if ( (bbox == null && newBbox != null) || (bbox !=null && !bbox.equals(newBbox)) ) {
            return new SearchQuery(
                endpoint,
                user,
                term,
                newBbox,
                spatialOperation,
                PAGE_DEFAULT,
                rows,
                facetFilters,
                groupStore,
                catalogue,
                facets
            );
        }
        else {
            return this;
        }
    }

    /**
     * Generate a search query with a new spatial operation. Changing a spatial
     * operation fundamentally changes the search query which means that we should
     * jump back to page 1.
     * @param newSpatialOperation the new spatial operation
     * @return a new query if spatial operation differs to the one set.
     */
    public SearchQuery withSpatialOperation(SpatialOperation newSpatialOperation) {
        if ( !spatialOperation.equals(newSpatialOperation) ) {
            return new SearchQuery(
                endpoint,
                user,
                term,
                bbox,
                newSpatialOperation,
                PAGE_DEFAULT,
                rows,
                facetFilters,
                groupStore,
                catalogue,
                facets
            );
        }
        else {
            return this;
        }
    }

    /**
     * Create a clone of this search query but apply the additional facet filter
     *
     * The logic of this method has been designed to match that of lombok's
     * @Wither methods.
     *
     * If the filter has already been applied, just return this search query
     * @param filter to ensure is present
     * @return a new search query or this one if no change is needed
     */
    @SuppressWarnings("JavaDoc")
    public SearchQuery withFacetFilter(FacetFilter filter) {
        if (!containsFacetFilter(filter)) {
            List<FacetFilter> newFacetFilters = new ArrayList<>(facetFilters);
            newFacetFilters.add(filter);
            return new SearchQuery(
                endpoint,
                user,
                term,
                bbox,
                spatialOperation,
                PAGE_DEFAULT,
                rows,
                newFacetFilters,
                groupStore,
                catalogue,
                facets
            );
        }
        else {
            return this;
        }
    }

    /**
     * Create a clone of this search query be ensure that the given facet filter
     * is not applied.
     *
     * If the filter is not applied, just return this search query
     * @param filter to ensure is missing
     * @return a new search query or this one if no change is needed
     */
    public SearchQuery withoutFacetFilter(FacetFilter filter) {
        if(containsFacetFilter(filter) ) {
            List<FacetFilter> newFacetFilters = new ArrayList<>(facetFilters);
            newFacetFilters.remove(filter);
            return new SearchQuery(
                endpoint,
                user,
                term,
                bbox,
                spatialOperation,
                PAGE_DEFAULT,
                rows,
                newFacetFilters,
                groupStore,
                catalogue,
                facets
            );
        }
        else {
            return this;
        }
    }

    /**
     * Check to see if the given facet filter is being applied by this search
     * query.
     * @param filter to see if it is being applied
     * @return true if it is false if it isn't
     */
    public boolean containsFacetFilter(FacetFilter filter) {
        return facetFilters.contains(filter);
    }

    /**
     * @return the url to call to perform this solr query.
     */
    public String toUrl() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(endpoint);
        if(PAGE_DEFAULT != page) {
            builder.queryParam(PAGE_QUERY_PARAM, page);
        }

        if(ROWS_DEFAULT != rows) {
            builder.queryParam(ROWS_QUERY_PARAM, rows);
        }

        if(!DEFAULT_SEARCH_TERM.equals(term)) {
            builder.queryParam(TERM_QUERY_PARAM, URLEncoder.encode(term, StandardCharsets.UTF_8));
        }

        if(bbox != null) {
            builder.queryParam(BBOX_QUERY_PARAM, bbox);
            builder.queryParam(OP_QUERY_PARAM, spatialOperation.getOperation());
        }

        if(!facetFilters.isEmpty()) {
            facetFilters.forEach((f)-> builder.queryParam(FACET_QUERY_PARAM, f.asURIContent()));
        }

        // cannot just encode UriComponents as other parameters (facets, bbox) already Uri encoded
        return builder.build().toUriString();
    }

    private void setSpatialFilter(SolrQuery query) {
        if(bbox != null) {
            query.addFilterQuery(String.format("locations:\"%s(%s)\"", spatialOperation.getOperation(), bbox));
        }
    }
    private void setRecordVisibility(SolrQuery query) {
        if (user.isPublic()) {
            query.addFilterQuery("{!term f=state}published");
            query.addFilterQuery("{!term f=view}public");
        } else {
            List<String> groups = groupStore.getGroups(user)
                .stream()
                .map(Group::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

            if ( !userIsPublisher(groups)) {
                query.addFilterQuery(userVisibility(groups));
            }
        }
    }

    private boolean userIsPublisher(List<String> groups) {
        return groups.contains(
            String.format(
                MetadataInfo.PUBLISHER_GROUP,
                catalogue.getId()
            ).toLowerCase()
        );
    }

    private String userVisibility(List<String> groups) {
        String username = user.getUsername().toLowerCase();
        StringBuilder toReturn = new StringBuilder("view:(public OR ")
            .append(username);

        groups
            .forEach(g -> toReturn
                .append(" OR ")
                .append(g));

        return toReturn.append(")").toString();
    }

    private void setFacetFilters(SolrQuery query){
        facetFilters.forEach((filter) ->
            query.addFilterQuery(filter.asSolrFilterQuery())
        );
    }

    private void setFacetFields(SolrQuery query){
        query.setFacet(true);
        query.setFacetMinCount(1);
        query.setFacetSort("index");
        facets.forEach((facet) ->
            query.addFacetField(facet.getFieldName())
        );
    }

    private void setCatalogueFilter(SolrQuery query) {
        query.addFilterQuery(
            String.format("{!term f=catalogue}%s", catalogue.getId())
        );
    }

    private void setSortOrder(SolrQuery query){
        if(DEFAULT_SEARCH_TERM.equals(term)){
            query.setSort(getRandomFieldName(), SolrQuery.ORDER.asc);
        }
    }

    private String getRandomFieldName(){
        Random randomGenerator = new Random(getRandomSeed());
        return RANDOM_DYNAMIC_FIELD_NAME + randomGenerator.nextInt();
    }

    private int getRandomSeed(){
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_YEAR);
    }
}
