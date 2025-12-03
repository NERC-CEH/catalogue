package uk.ac.ceh.gateway.catalogue.services;

import lombok.RequiredArgsConstructor;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResourceIdentifierLookupService {

    private final SolrClient solrClient;

    public Optional<String> resolveToUuid(String identifier) {
        return lookup(identifier);
    }

    public Optional<String> findDocumentByRi(String identifier) {
        return lookup(identifier);
    }

    private Optional<String> lookup(String identifier) {
        try {
            SolrQuery query = new SolrQuery();
            query.setQuery("resourceIdentifier:\"" + identifier + "\"");
            query.setRows(1);

            QueryResponse response = solrClient.query("documents", query);

            if (response.getResults().isEmpty()) {
                return Optional.empty();
            }

            String uuid = (String) response.getResults().getFirst().getFieldValue("identifier");

            return Optional.ofNullable(uuid);
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }
}
