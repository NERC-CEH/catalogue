package uk.ac.ceh.gateway.catalogue.vocabularies;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.nio.charset.StandardCharsets.UTF_8;
import static uk.ac.ceh.gateway.catalogue.TimeConstants.ONE_MINUTE;
import static uk.ac.ceh.gateway.catalogue.TimeConstants.SEVEN_DAYS;

@Slf4j
@ToString(exclude = {"restTemplate", "solrClient"})
public class SparqlKeywordVocabulary implements KeywordVocabulary {
    private final String vocabularyId;
    private final String vocabularyName;
    private final List<String> catalogueIds;
    private final RestTemplate restTemplate;
    private final SolrClient solrClient;
    private final URI queryUrl;

    private static final String COLLECTION = "keywords";

    public SparqlKeywordVocabulary(
            RestTemplate restTemplate,
            SolrClient solrClient,
            String sparqlEndpoint,
            String graph,
            String where,
            String vocabularyId,
            String vocabularyName,
            List<String> catalogueIds
    ) {
        this.restTemplate = restTemplate;
        this.solrClient = solrClient;
        this.vocabularyId = vocabularyId;
        this.catalogueIds = catalogueIds;
        this.vocabularyName = vocabularyName;
        this.queryUrl = createQueryUrl(sparqlEndpoint, graph, where);
        log.info("Creating {}", this);
    }

    @SuppressWarnings("HttpUrlsUsage")
    private URI createQueryUrl(String sparqlEndpoint, String graph, String where) {
        return URI.create(
            sparqlEndpoint + "?query=" +
            URLEncoder.encode("PREFIX skos:<http://www.w3.org/2004/02/skos/core#> ", UTF_8) +
            URLEncoder.encode("SELECT ?uri ?label ", UTF_8) +
            URLEncoder.encode("WHERE {GRAPH <", UTF_8) +
            URLEncoder.encode(graph, UTF_8) +
            URLEncoder.encode("> {", UTF_8) +
            URLEncoder.encode(where, UTF_8) +
            URLEncoder.encode("}}", UTF_8) +
            "&format=json-simple"
        );
    }

    @Override
    @SneakyThrows
    @Scheduled(initialDelay = ONE_MINUTE, fixedDelay = SEVEN_DAYS)
    public void retrieve() {
        log.info("Retrieving vocabulary ({}) {}", vocabularyId, vocabularyName);
        val response = restTemplate.getForEntity(
            queryUrl,
            JsonNode.class
        );
        log.debug(response.toString());
        val vocabularyNode = Optional.ofNullable(response.getBody())
            .orElseThrow(() -> new KeywordVocabularyException("Cannot get response body"));

        if (vocabularyNode.isArray()) {
            solrClient.deleteByQuery(COLLECTION, "vocabId:" + vocabularyId);
            StreamSupport.stream(vocabularyNode.spliterator(), false)
                .map(node -> {
                    val url = node.get("uri").asText();
                    val label = node.get("label").asText();
                    return new Keyword(label, vocabularyId, url);
                })
                .forEach(keyword -> {
                    try {
                        solrClient.addBean(COLLECTION, keyword);
                    } catch (IOException | SolrServerException ex) {
                        throw new KeywordVocabularyException("Failed to index " + keyword + "for " + vocabularyId, ex);
                    }
                });
            try {
                solrClient.commit(COLLECTION);
            } catch (IOException | SolrServerException ex) {
                throw new KeywordVocabularyException("Failed to commit keywords for " + vocabularyId, ex);
            }
        }
    }

    @Override
    public String getName() {
        return vocabularyName;
    }

    @Override
    public String getId() {
        return vocabularyId;
    }

    @Override
    public boolean usedInCatalogue(String catalogueId) {
        return catalogueIds.contains(catalogueId);
    }
}

