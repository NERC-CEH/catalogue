package uk.ac.ceh.gateway.catalogue.services;

import lombok.RequiredArgsConstructor;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResourceIdentifierLookupService {

    /**
     * Upper bound on the number of documents we will inspect for a single identifier. Identifiers
     * are expected to be unique, so a handful of hits is the realistic ceiling; the cap guards
     * against pathological cases without paging.
     */
    private static final int MAX_OWNERS = 100;

    private final SolrClient solrClient;

    /**
     * Resolve an identifier to a single owning document id. Intended for redirect/resolution where
     * one identifier maps to one record (see IdController).
     */
    public Optional<String> resolveToUuid(String identifier) {
        List<String> owners = findDocumentIdsByRi(identifier);
        return owners.isEmpty() ? Optional.empty() : Optional.of(owners.getFirst());
    }

    /**
     * Find the ids of every document that holds the given resource identifier exactly. Used by the
     * save-time uniqueness check, which must see all owners (not just the top-ranked hit) so it can
     * exclude the record being saved before deciding a duplicate exists.
     *
     * <p>The query targets {@code resourceIdentifierExact}, an un-analyzed (KeywordTokenizer +
     * lowercase) field, so matching is exact and case-insensitive — unlike the analyzed
     * {@code resourceIdentifier} field used for free-text search, which tokenizes and stems and
     * cannot be relied on for identity comparison.
     */
    public List<String> findDocumentIdsByRi(String identifier) {
        try {
            SolrQuery query = new SolrQuery();
            query.setQuery("resourceIdentifierExact:\"" + escape(identifier) + "\"");
            query.setFields("identifier");
            query.setRows(MAX_OWNERS);

            QueryResponse response = solrClient.query("documents", query);

            return response.getResults().stream()
                .map(doc -> (String) doc.getFieldValue("identifier"))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        }
        catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Escape characters that would break out of the quoted phrase in a Solr query.
     */
    private String escape(String identifier) {
        return identifier.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
