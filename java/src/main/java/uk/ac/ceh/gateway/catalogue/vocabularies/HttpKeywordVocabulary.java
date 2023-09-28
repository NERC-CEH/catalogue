package uk.ac.ceh.gateway.catalogue.vocabularies;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.HttpStatusCodeException;

import static java.lang.String.format;

import static uk.ac.ceh.gateway.catalogue.TimeConstants.ONE_MINUTE;
import static uk.ac.ceh.gateway.catalogue.TimeConstants.SEVEN_DAYS;

@Slf4j
@ToString(exclude = "solrClient")
public class HttpKeywordVocabulary implements KeywordVocabulary {
    private final String vocabularyId;
    private final String vocabularyName;
    private final String httpEndpoint;
    private final List<String> catalogueIds;
    private final SolrClient solrClient;
    private final ObjectMapper objectMapper;
    private final JsonPointer resultsArrayPointer;
    private final JsonPointer uriPointer;
    private final JsonPointer labelPointer;

    private static final String COLLECTION = "keywords";

    public HttpKeywordVocabulary(
            String vocabularyId,
            String vocabularyName,
            String httpEndpoint,
            String RESULTS,
            String URIS,
            String LABELS,
            SolrClient solrClient,
            List<String> catalogueIds
    ) {
        this.vocabularyId = vocabularyId;
        this.vocabularyName = vocabularyName;
        this.httpEndpoint = httpEndpoint;
        this.catalogueIds = catalogueIds;
        this.solrClient = solrClient;
        this.objectMapper = new ObjectMapper();
        resultsArrayPointer = JsonPointer.compile(RESULTS);
        uriPointer = JsonPointer.compile(URIS);
        labelPointer = JsonPointer.compile(LABELS);
        log.info("Creating {}", this);
    }

    @Override
    @SneakyThrows
    @Scheduled(initialDelay = ONE_MINUTE, fixedDelay = SEVEN_DAYS)
    public void retrieve() {
        log.info("Retrieving vocabulary ({}) {}", vocabularyId, vocabularyName);
        try {
            val vocabularyNode = Optional.ofNullable(objectMapper.readTree(new URL(httpEndpoint)))
                .orElseThrow(() -> new KeywordVocabularyException("Cannot get response body"))
                .at(resultsArrayPointer);
            log.debug(vocabularyNode.toString());

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
            log.error(format("Cannot retrieve %s from vocab server, error: %s %s", vocabularyId, ex.getRawStatusCode(), ex.getResponseBodyAsString()));
            throw new KeywordVocabularyException(
                format("Cannot retrieve %s from vocab server, error: %s %s", vocabularyId, ex.getRawStatusCode(), ex.getResponseBodyAsString()),
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
        return "N/A";
    }

    @Override
    public boolean usedInCatalogue(String catalogueId) {
        return catalogueIds.contains(catalogueId);
    }
}
