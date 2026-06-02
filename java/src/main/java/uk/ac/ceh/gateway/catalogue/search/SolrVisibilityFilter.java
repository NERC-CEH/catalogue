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
 * Package-private utility — applies Solr record-visibility filter queries for a given user.
 * Shared by SemanticSearcher and HybridSearcher to avoid duplicating the publisher/group logic.
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
