package uk.ac.ceh.gateway.catalogue.search;

import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import org.apache.solr.client.solrj.request.SolrQuery;

import java.util.List;

public interface Searcher {
    SearchResults search(
        String endpoint,
        CatalogueUser user,
        String term,
        String bbox,
        SpatialOperation spatialOperation,
        int page,
        int rows,
        List<FacetFilter> facetFilters,
        String catalogueKey,
        String sortField,
        SolrQuery.ORDER sortOrder
    );
}
