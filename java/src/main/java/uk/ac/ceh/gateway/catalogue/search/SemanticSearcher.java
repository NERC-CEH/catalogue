package uk.ac.ceh.gateway.catalogue.search;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.userstore.Group;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

@Slf4j
@Service
@ConditionalOnBean(EmbeddingModel.class)
public class SemanticSearcher {

    private final EmbeddingModel embeddingModel;
    private final SolrClient solrClient;
    private final GroupStore<CatalogueUser> groupStore;
    private final CatalogueService catalogueService;

    public SemanticSearcher(
            EmbeddingModel embeddingModel,
            SolrClient solrClient,
            GroupStore<CatalogueUser> groupStore,
            CatalogueService catalogueService
    ) {
        this.embeddingModel = embeddingModel;
        this.solrClient = solrClient;
        this.groupStore = groupStore;
        this.catalogueService = catalogueService;
        log.info("Creating");
    }

    @SneakyThrows
    public SearchResults search(
            String endpoint,
            CatalogueUser user,
            String term,
            String bbox,
            SpatialOperation spatialOperation,
            int page,
            int rows,
            String catalogueKey
    ) {
        val catalogue = catalogueService.retrieve(catalogueKey);

        float[] vec = embeddingModel.embed(term);
        String vectorStr = buildVectorString(vec);

        SolrQuery query = new SolrQuery("{!knn f=vector topK=" + rows + "}" + vectorStr)
                .setStart((page - 1) * rows)
                .setRows(rows);

        if (bbox != null) {
            query.addFilterQuery(String.format("locations:\"%s(ENVELOPE(%s))\"",
                    spatialOperation.getOperation(), bbox));
        }
        applyVisibilityFilters(query, user, catalogue.getId(), catalogueKey);
        if (!CatalogueService.ALL_CATALOGUES_ID.equals(catalogueKey)) {
            query.addFilterQuery("{!term f=catalogue}" + catalogueKey);
        }

        log.debug("Semantic query: {}", query);

        val response = solrClient.query("documents", query, POST);

        // Construct a minimal SearchQuery for SearchResults pagination URLs
        val searchQuery = new SearchQuery(
                endpoint, user, term, bbox, spatialOperation,
                page, rows,
                Collections.emptyList(),
                groupStore, catalogue,
                Collections.emptyList(),
                null, SolrQuery.ORDER.asc
        );
        return new SearchResults(response, searchQuery, Collections.emptyList());
    }

    private void applyVisibilityFilters(SolrQuery query, CatalogueUser user, String catalogueId, String catalogueKey) {
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

    private boolean isPublisher(List<String> groups, String catalogueId, String catalogueKey) {
        if (CatalogueService.ALL_CATALOGUES_ID.equals(catalogueKey)) {
            return false;
        }
        return groups.contains(
                String.format(MetadataInfo.PUBLISHER_GROUP, catalogueId).toLowerCase()
        );
    }

    private String buildUserVisibilityFilter(CatalogueUser user, List<String> groups) {
        StringBuilder filter = new StringBuilder("view:(public OR ")
                .append(user.getUsername().toLowerCase());
        groups.forEach(g -> filter.append(" OR ").append(g));
        return filter.append(")").toString();
    }

    private String buildVectorString(float[] vec) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vec.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(vec[i]);
        }
        return sb.append(']').toString();
    }
}
