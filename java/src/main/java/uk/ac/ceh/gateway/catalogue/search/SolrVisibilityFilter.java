package uk.ac.ceh.gateway.catalogue.search;

import lombok.val;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.util.ClientUtils;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Applies the Solr filter queries that scope a search: record visibility for a given user, and the
 * catalogue being searched. Shared by SolrSearcher, SemanticSearcher and the editor's people look-up
 * (which must not offer a name from a draft its user can't see) so the publisher/group logic and the
 * catalogue expression exist in one place.
 */
public final class SolrVisibilityFilter {

    private SolrVisibilityFilter() {}

    public static void apply(
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
     * Record visibility for a query across every catalogue, as the editor's people look-up makes: what
     * the user and their groups can see, and every record in each catalogue they publish, as
     * {@link #apply} gives a publisher when searching that one catalogue. Without the second part an
     * EIDC publisher would be offered nothing from an EIDC record shared only with a project's group,
     * though EIDC's own search shows them it (dri-one #534).
     */
    public static void applyAcrossCatalogues(SolrQuery query, CatalogueUser user, GroupStore<CatalogueUser> groupStore) {
        if (user.isPublic()) {
            apply(query, user, groupStore, CatalogueService.ALL_CATALOGUES_ID, CatalogueService.ALL_CATALOGUES_ID);
            return;
        }
        List<String> groups = groupStore.getGroups(user)
                .stream()
                .map(Group::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        val published = groups.stream()
                .map(PUBLISHER::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group(1))
                .map(ClientUtils::escapeQueryChars)
                .toList();
        val filter = new StringBuilder(buildUserVisibilityFilter(user, groups));
        published.forEach(catalogue -> filter.append(" OR catalogue:").append(catalogue)
                .append(" OR catalogue_view:").append(catalogue));
        query.addFilterQuery(filter.toString());
    }

    /** role_eidc_publisher: the group whose members publish a catalogue, lower-cased as groups are compared */
    private static final Pattern PUBLISHER = Pattern.compile(
            String.format(MetadataInfo.PUBLISHER_GROUP, "([a-z0-9-]+)").toLowerCase());

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
