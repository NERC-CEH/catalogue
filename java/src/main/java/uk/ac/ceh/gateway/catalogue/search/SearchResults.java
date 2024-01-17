package uk.ac.ceh.gateway.catalogue.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.indexing.solr.SolrIndex;
import uk.ac.ceh.gateway.catalogue.model.Link;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A search results object for documents
 */
@ConvertUsing({
        @Template(called = "/html/search.ftlh", whenRequestedAs = MediaType.TEXT_HTML_VALUE)
})
@Value
@Slf4j
public class SearchResults {

    long numFound;
    String term;
    int page;
    int rows;
    String url;
    String withoutBbox;
    String intersectingBbox;
    String withinBbox;
    String prevPage;
    String nextPage;
    List<SolrIndex> results;
    List<Facet> facets;
    @JsonIgnore
    Catalogue catalogue;
    List<Link> relatedSearches;

    public SearchResults(QueryResponse response, SearchQuery query, List<Link> relatedSearches) {
        checkNotNull(response);
        checkNotNull(query);
        this.numFound = populateNumFound(response);
        this.term = query.getTermNotDefault();
        this.page = query.getPage();
        this.rows = query.getRows();
        this.url = query.toUrl();
        this.withoutBbox = populateWithoutBbox(query);
        this.intersectingBbox = populateIntersectingBbox(query);
        this.withinBbox = populateWithinBbox(query);
        this.prevPage = populatePrevPage(query);
        this.nextPage = populateNextPage(query);
        this.results = response.getBeans(SolrIndex.class);
        this.facets = populateFacets(response, query);
        this.catalogue = query.getCatalogue();
        this.relatedSearches = relatedSearches;
        log.debug("Creating: {}", this);
    }

    public SearchResults(SearchResults searchResults, List<Link> relatedSearches) {
        this.numFound = searchResults.numFound;
        this.term = searchResults.term;
        this. page = searchResults.page;
        this.rows = searchResults.rows;
        this.url = searchResults.url;
        this.withoutBbox = searchResults.withoutBbox;
        this.intersectingBbox = searchResults.intersectingBbox;
        this.withinBbox = searchResults.withinBbox;
        this.prevPage = searchResults.prevPage;
        this.nextPage = searchResults.nextPage;
        this.results = searchResults.results;
        this.facets = searchResults.facets;
        this.catalogue = searchResults.catalogue;
        this.relatedSearches = relatedSearches;
    }

    SearchResults(
            int numFound,
            String term,
            int page,
            int rows,
            String url,
            String withoutBbox,
            String intersectingBbox,
            String withinBbox,
            String prevPage,
            String nextPage,
            List<SolrIndex> results,
            List<Facet> facets,
            Catalogue catalogue,
            List<Link> relatedSearches
            ) {
        this.numFound = numFound;
        this.term = term;
        this.page = page;
        this.rows = rows;
        this.url = url;
        this.withoutBbox = withoutBbox;
        this.intersectingBbox = intersectingBbox;
        this.withinBbox = withinBbox;
        this.prevPage = prevPage;
        this.nextPage = nextPage;
        this.results = results;
        this.facets = facets;
        this.catalogue = catalogue;
        this.relatedSearches = relatedSearches;
        log.debug("Creating: {}", this);
            }

    private long populateNumFound(QueryResponse response) {
        return Optional.ofNullable(response.getResults())
            .map(SolrDocumentList::getNumFound)
            .orElse(0L);
    }

    /**
     * Return a link to the previous page as long as we are not currently on the
     * first page.
     * @return url to the previous page or null.
     */
    private String populatePrevPage(SearchQuery query) {
        if(page != 1) {
            return query.withPage(getPage() - 1).toUrl();
        }
        else {
            return null;
        }
    }

    /**
     * Return a link to the next page as long as there is a page to go to.
     * @return A link to the next page if there is one to go to.
     */
    private String populateNextPage(SearchQuery query) {
        if(numFound > (long) page * rows) {
            return query.withPage(getPage() + 1).toUrl();
        }
        else {
            return null;
        }
    }

    /**
     * Return a link to a search which is not applying the applied bounding box
     * filter
     * @return url to a search without the bbox component
     */
    private String populateWithoutBbox(SearchQuery query) {
        if(query.getBbox() != null) {
            return query.withBbox(null).toUrl();
        }
        else {
            return null;
        }
    }

    private String populateIntersectingBbox(SearchQuery query) {
        if(query.getBbox() != null && query.getSpatialOperation() != SpatialOperation.INTERSECTS) {
            return query.withSpatialOperation(SpatialOperation.INTERSECTS).toUrl();
        }
        else {
            return null;
        }
    }

    private String populateWithinBbox(SearchQuery query) {
        if(query.getBbox() != null && query.getSpatialOperation() != SpatialOperation.ISWITHIN) {
            return query.withSpatialOperation(SpatialOperation.ISWITHIN).toUrl();
        }
        else {
            return null;
        }
    }

    private List<Facet> populateFacets(QueryResponse response, SearchQuery query){
        List<Facet> newFacets = query.getFacets();

        newFacets.forEach((facet) -> {
            FacetField facetField = response.getFacetField(facet.getFieldName());
            if (facetField != null) {
                if (facet.isHierarchical()) {
                    facet.getResults().addAll(getHierarchicalFacetResults(query, facetField, "0/"));
                } else {
                    facet.getResults().addAll(getFacetResults(query, facetField));
                }
            }
        });
        return newFacets;
    }

    private List<FacetResult> getFacetResults(SearchQuery query, FacetField facetField) {
        return facetField.getValues().stream()
            .map(count -> {
                String field = facetField.getName();
                String name = count.getName();
                FacetFilter filter = new FacetFilter(field, name);
                boolean active = query.containsFacetFilter(filter);
                return FacetResult.builder()
                    .name(name)
                    .count(count.getCount())
                    .active(active)
                    .url(((active) ? query.withoutFacetFilter(filter) : query.withFacetFilter(filter)).toUrl())
                    .build();
            })
        .collect(Collectors.toList());
    }

    private List<FacetResult> getHierarchicalFacetResults(SearchQuery query, FacetField facetField, String prefix) {
        return facetField.getValues().stream()
            .filter(c -> c.getName().startsWith(prefix))
            .map(c -> getFacetResultFromCount(query, c))
            .collect(Collectors.toList());
    }

    private FacetResult getFacetResultFromCount(SearchQuery query, FacetField.Count count) {
        String name = count.getName();
        FacetFilter filter = new FacetFilter(count.getFacetField().getName(), name);
        boolean active = query.containsFacetFilter(filter);
        return FacetResult.builder()
            .name(getName(name))
            .count(count.getCount())
            .active(active)
            .url(((active) ? query.withoutFacetFilter(filter) : query.withFacetFilter(filter)).toUrl())
            .subFacetResults(getHierarchicalFacetResults(query, count.getFacetField(), getChildName(name)))
            .build();
    }

    private String getChildName(String name) {
        int i = name.indexOf("/");
        Integer child = Integer.parseInt(name.substring(0, i)) + 1;
        return child + name.substring(i);
    }

    private String getName(String name) {
        int last = name.length() - 1;
        int i = name.lastIndexOf("/", last - 1) + 1;
        return name.substring(i, last);
    }
}
