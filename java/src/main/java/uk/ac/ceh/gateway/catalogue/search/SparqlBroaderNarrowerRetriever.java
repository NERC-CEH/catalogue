package uk.ac.ceh.gateway.catalogue.search;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.vocabularies.Keyword;
import uk.ac.ceh.gateway.catalogue.vocabularies.KeywordVocabularyException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Profile("search:enhanced")
@Service
public class SparqlBroaderNarrowerRetriever  implements BroaderNarrowerRetriever {
    private final RestTemplate restTemplate;
    private final String sparqlEndpoint;

    public SparqlBroaderNarrowerRetriever(
        @Qualifier("sparql") RestTemplate restTemplate,
        @Value("${sparql.endpoint}") String sparqlEndpoint
    ) {
        this.restTemplate = restTemplate;
        this.sparqlEndpoint = sparqlEndpoint;
    }

    @Override
    public List<Link> retrieve(Keyword keyword) {
        // create sparql query
        val request = """
            
            """;
        val response = restTemplate.postForEntity(
            sparqlEndpoint,
            request,
            JsonNode.class
        );
        val vocabularyNode = Optional.ofNullable(response.getBody())
            .orElseThrow(() -> new KeywordVocabularyException("Cannot get response body"));
        // convert results to a list of links
        return Collections.emptyList();
    }
}
