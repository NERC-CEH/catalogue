package uk.ac.ceh.gateway.catalogue.search;

import org.apache.solr.client.solrj.request.SolrQuery;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Package-private utility — applies the Solr filter queries that scope a search: record visibility
 * for a given user, and the catalogue being searched. Shared by SolrSearcher and SemanticSearcher
 * so the publisher/group logic and the catalogue expression exist in one place.
 */
final class SolrVisibilityFilter {

    private SolrVisibilityFilter() {}

    static void apply(
            SolrQuery query,
            CatalogueUser user,
            GroupStore<CatalogueUser> groupStore,
            String catalogueId,
            String catalogueKey
    ) {
        if (user.isPublic()) {
            query.addFilterQuery("{!term f=state}published");
            query.addFilterQuery("{!term f=view}public");
            query.addFilterQuery("NOT availability:(Superseded OR Withdrawn)");
        } else {
            List<String> groups = groupStore.getGroups(user)
                    .stream()
                    .map(Group::getName)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());

            if (!isPublisher(groups, catalogueId, catalogueKey)) {
                query.addFilterQuery(buildUserVisibilityFilter(user, groups));
            }
        }
    }

    /**
     * Restricts results to a single catalogue.
     * <p>
     * A record shared into a catalogue carries that catalogue in {@code catalogue_view} rather than
     * {@code catalogue}, so filtering on {@code catalogue} alone hides exactly those records: they
     * appear in ordinary search and silently vanish from semantic search. Both fields have to be
     * considered, and {@link SearchQuery} delegates here so the two search paths cannot drift apart
     * again — a duplicated {@code qf} string has already broken that way once.
     * <p>
     * The id is expected to come from a resolved {@code Catalogue} rather than straight off the
     * request: it is interpolated into a parsed query, so it must be a configured key and not
     * arbitrary text. {@code CatalogueService.retrieve} rejects anything else.
     */
    static void applyCatalogueScope(SolrQuery query, String catalogueId) {
        if (CatalogueService.ALL_CATALOGUES_ID.equals(catalogueId)) {
            return;
        }
        query.addFilterQuery(
                String.format("(catalogue:%s OR catalogue_view:%s)", catalogueId, catalogueId)
        );
    }

    private static boolean isPublisher(List<String> groups, String catalogueId, String catalogueKey) {
        if (CatalogueService.ALL_CATALOGUES_ID.equals(catalogueKey)) {
            return false;
        }
        return groups.contains(
                String.format(MetadataInfo.PUBLISHER_GROUP, catalogueId).toLowerCase()
        );
    }

    private static String buildUserVisibilityFilter(CatalogueUser user, List<String> groups) {
        StringBuilder filter = new StringBuilder("view:(public OR ")
                .append(user.getUsername().toLowerCase());
        groups.forEach(g -> filter.append(" OR ").append(g));
        return filter.append(")").toString();
    }
}
