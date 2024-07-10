package uk.ac.ceh.gateway.catalogue.vocabularies;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static uk.ac.ceh.gateway.catalogue.TimeConstants.ONE_MINUTE;
import static uk.ac.ceh.gateway.catalogue.TimeConstants.SEVEN_DAYS;

@Slf4j
@ToString(exclude = "solrClient")
public class HttpKeywordVocabulary implements KeywordVocabulary {
    private final String vocabularyId;
    private final String vocabularyName;
    private final List<String> httpEndpoints;
    private final List<String> catalogueIds;
    private final SolrClient solrClient;
    private final ObjectMapper objectMapper;
    private final JsonPointer resultsArrayPointer;
    private final JsonPointer uriPointer;
    private final JsonPointer labelPointer;

    private static final String COLLECTION = "keywords";

    // single endpoint constructor
    public HttpKeywordVocabulary(
            String vocabularyId,
            String vocabularyName,
            String httpEndpoint,
            String resultsPath,
            String uriPath,
            String labelPath,
            SolrClient solrClient,
            List<String> catalogueIds
    ) {
        this.vocabularyId = vocabularyId;
        this.vocabularyName = vocabularyName;
        this.httpEndpoints = List.of(httpEndpoint);
        this.catalogueIds = catalogueIds;
        this.solrClient = solrClient;
        this.objectMapper = new ObjectMapper();
        resultsArrayPointer = JsonPointer.compile(resultsPath);
        uriPointer = JsonPointer.compile(uriPath);
        labelPointer = JsonPointer.compile(labelPath);
        log.info("Creating {}", this);
    }

    // multiple endpoint constructor
    public HttpKeywordVocabulary(
            String vocabularyId,
            String vocabularyName,
            List<String> httpEndpoints,
            String resultsPath,
            String uriPath,
            String labelPath,
            SolrClient solrClient,
            List<String> catalogueIds
    ) {
        this.vocabularyId = vocabularyId;
        this.vocabularyName = vocabularyName;
        this.httpEndpoints = httpEndpoints;
        this.catalogueIds = catalogueIds;
        this.solrClient = solrClient;
        this.objectMapper = new ObjectMapper();
        resultsArrayPointer = JsonPointer.compile(resultsPath);
        uriPointer = JsonPointer.compile(uriPath);
        labelPointer = JsonPointer.compile(labelPath);
        log.info("Creating {}", this);
    }

    @Override
    @SneakyThrows
    @Scheduled(initialDelay = ONE_MINUTE, fixedDelay = SEVEN_DAYS)
    public void retrieve() {
        log.info("Retrieving vocabulary ({}) {}", vocabularyId, vocabularyName);

        // not ideal always deleting because of the possibility of errors
        // but can add logic around this later
        solrClient.deleteByQuery(COLLECTION, "vocabId:" + vocabularyId);
        for (String endpoint : httpEndpoints){
            try {
                val vocabularyNode = Optional.ofNullable(objectMapper.readTree(new URI(endpoint).toURL()))
                    .orElseThrow(() -> new KeywordVocabularyException("Cannot get response body"))
                    .at(resultsArrayPointer);
                log.debug(vocabularyNode.toString());

                if (vocabularyNode.isArray()) {
                    log.info("Retrieved {} terms", vocabularyNode.size());
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
                }
            } catch (HttpStatusCodeException ex) {
                log.error(format("Cannot retrieve %s from vocab server, error: %s %s", vocabularyId, ex.getStatusCode().value(), ex.getResponseBodyAsString()));
                throw new KeywordVocabularyException(
                        format("Cannot retrieve %s from vocab server, error: %s %s", vocabularyId, ex.getStatusCode().value(), ex.getResponseBodyAsString()),
                        ex
                        );
            } catch (IOException | URISyntaxException ex) {
                throw new KeywordVocabularyException(
                        format("Failed to communicate with Solr for %s", vocabularyId),
                        ex
                        );
            }
        }
        solrClient.commit(COLLECTION);
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
        return "N/A";
    }

    @Override
    public boolean usedInCatalogue(String catalogueId) {
        return catalogueIds.contains(catalogueId);
    }
}
