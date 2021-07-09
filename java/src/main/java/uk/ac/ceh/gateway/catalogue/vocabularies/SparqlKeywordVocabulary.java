package uk.ac.ceh.gateway.catalogue.vocabularies;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.TimeConstants;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.nio.charset.StandardCharsets.UTF_8;

@Profile("elter")
@Slf4j
@ToString(of = {"address", "catalogueIds", "vocabularyId"})
public class SparqlKeywordVocabulary implements KeywordVocabulary {

    private String graph;
    private String where;
    private String vocabularyId;
    private List<String> catalogueIds;
    private final RestTemplate restTemplate;
    private final SolrClient solrClient;
    private final String sparqlEndpoint;
    private static final String KEYWORDS = "keywords";
    private String vocabularyName;

    public SparqlKeywordVocabulary(
            @Qualifier("normal") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${sparql.endpoint}") String sparqlEndpoint,
            String graph,
            String where,
            String vocabularyId,
            String vocabularyName,
            List<String> catalogueIds
    ) {
        this.restTemplate = restTemplate;
        this.solrClient = solrClient;
        this.graph = graph;
        this.where = where;
        this.sparqlEndpoint = sparqlEndpoint;
        this.vocabularyId = vocabularyId;
        this.catalogueIds = catalogueIds;
        this.vocabularyName = vocabularyName;
        log.info("Creating {}", this);
    }

    @Override
    @SneakyThrows
    @Scheduled(initialDelay = TimeConstants.ONE_MINUTE, fixedDelay = TimeConstants.SEVEN_DAYS)
    public void retrieve() {
        log.info("Re-indexing" + vocabularyId + "Vocabularies");
        val response = restTemplate.getForEntity(
                URI.create(
                        sparqlEndpoint + "?query=" +
                        URLEncoder.encode("PREFIX skos:<http://www.w3.org/2004/02/skos/core#> ", UTF_8) +
                        URLEncoder.encode("SELECT ?uri ?label ", UTF_8) +
                        URLEncoder.encode("WHERE {GRAPH <", UTF_8) +
                        URLEncoder.encode(graph, UTF_8) +
                        URLEncoder.encode("> {", UTF_8) +
                        URLEncoder.encode(where, UTF_8) +
                        URLEncoder.encode("}}", UTF_8) +
                        "&format=json-simple"

                ),
                JsonNode.class
        );

        log.debug(response.toString());
        val vocabularyNode = Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new KeywordVocabularyException("Cannot get response body"))
                .at("/results/bindings");

        if (vocabularyNode.isArray()) {
            solrClient.deleteByQuery(KEYWORDS, "vocabId:" + vocabularyId);
            StreamSupport.stream(vocabularyNode.spliterator(), false)
                    .map(node -> {
                        val uri = node.get("uri").asText();
                        val label = node.get("label").asText();
                        return new VocabularyKeyword(uri, label, vocabularyId);
                    }).forEach(keyword -> {
                try {
                    solrClient.addBean(KEYWORDS, keyword);
                } catch (IOException | SolrServerException ex) {
                    throw new KeywordVocabularyException("Failed to index " + keyword, ex);
                }
            });
        }
    }

    @Override
    public String getName() {
        return vocabularyName;
    }

    @Override
    public List<String> getCatalogues() {
        return catalogueIds;
    }
}

