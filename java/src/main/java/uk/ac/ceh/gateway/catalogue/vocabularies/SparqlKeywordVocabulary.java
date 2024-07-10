package uk.ac.ceh.gateway.catalogue.vocabularies;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static uk.ac.ceh.gateway.catalogue.TimeConstants.ONE_MINUTE;
import static uk.ac.ceh.gateway.catalogue.TimeConstants.SEVEN_DAYS;

@Slf4j
@ToString(exclude = {"restTemplate", "solrClient"})
public class SparqlKeywordVocabulary implements KeywordVocabulary {
    private final String vocabularyId;
    private final String vocabularyName;
    private final String graph;
    private final List<String> catalogueIds;
    private final RestTemplate restTemplate;
    private final SolrClient solrClient;
    private final URI queryUrl;

    public static final String COLLECTION = "keywords";
    public static final JsonPointer bindingsPointer = JsonPointer.compile("/results/bindings");
    public static final JsonPointer uriPointer = JsonPointer.compile("/uri/value");
    public static final JsonPointer labelPointer = JsonPointer.compile("/label/value");

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
        this.graph = graph;
        this.queryUrl = createQueryUrl(sparqlEndpoint, graph, where);
        log.info("Creating {}", this);
    }

    private URI createQueryUrl(String sparqlEndpoint, String graph, String where) {
        return URI.create(
            sparqlEndpoint + "?query=" +
            URLEncoder.encode("PREFIX skos:<http://www.w3.org/2004/02/skos/core#> ", UTF_8) +
            URLEncoder.encode("SELECT ?uri ?label ", UTF_8) +
            URLEncoder.encode("WHERE {GRAPH ", UTF_8) +
            URLEncoder.encode(graph, UTF_8) +
            URLEncoder.encode(" {", UTF_8) +
            URLEncoder.encode(where, UTF_8) +
            URLEncoder.encode("}}", UTF_8)
        );
    }

    @Override
    @SneakyThrows
    @Scheduled(initialDelay = ONE_MINUTE, fixedDelay = SEVEN_DAYS)
    public void retrieve() {
        log.info("Retrieving vocabulary ({}) {}", vocabularyId, vocabularyName);
        try {
            val response = restTemplate.getForEntity(
                queryUrl,
                JsonNode.class
            );
            log.debug(response.toString());
            val vocabularyNode = Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new KeywordVocabularyException("Cannot get response body"))
                .at(bindingsPointer);

            if (vocabularyNode.isArray()) {
                log.info("Retrieved {} terms", vocabularyNode.size());
                solrClient.deleteByQuery(COLLECTION, "vocabId:" + vocabularyId);
                StreamSupport.stream(vocabularyNode.spliterator(), false)
                    .map(node -> {
                        val url = node.at(uriPointer).asText();
                        val label = node.at(labelPointer).asText();
                        return new Keyword(label, vocabularyId, url);
                    })
                    .forEach(keyword -> {
                        try {
                            solrClient.addBean(COLLECTION, keyword);
                        } catch (IOException | SolrServerException ex) {
                            throw new KeywordVocabularyException("Failed to index " + keyword + "for " + vocabularyId, ex);
                        }
                    });
                solrClient.commit(COLLECTION);
            }
        } catch (HttpStatusCodeException ex) {
            log.error(format("Cannot retrieve %s from vocab server, error: %s %s", vocabularyId, ex.getStatusCode().value(), ex.getResponseBodyAsString()));
            throw new KeywordVocabularyException(
                format("Cannot retrieve %s from vocab server, error: %s %s", vocabularyId, ex.getStatusCode().value(), ex.getResponseBodyAsString()),
                ex
            );
        } catch (IOException | SolrServerException ex) {
            throw new KeywordVocabularyException(
                format("Failed to communicate with Solr for %s", vocabularyId),
                ex
            );
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
    public String getGraph() {
        return graph;
    }

    @Override
    public boolean usedInCatalogue(String catalogueId) {
        return catalogueIds.contains(catalogueId);
    }
}

