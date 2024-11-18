package uk.ac.ceh.gateway.catalogue.vocabularies;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static uk.ac.ceh.gateway.catalogue.TimeConstants.ONE_MINUTE;
import static uk.ac.ceh.gateway.catalogue.TimeConstants.SEVEN_DAYS;

/**
 * LocalKeywordVocabulary is responsible for loading vocabulary data from a local file and indexing it in Solr. The class reads the JSON file and processes the terms to store them in the Solr index.
 */
@Slf4j
@ToString(exclude = "solrClient")
public class LocalKeywordVocabulary implements KeywordVocabulary {
    private final String vocabularyId;
    private final String vocabularyName;
    private final String filePath;
    private final List<String> catalogueIds;
    private final SolrClient solrClient;
    private final ObjectMapper objectMapper;
    private final JsonPointer resultsArrayPointer;
    private final JsonPointer uriPointer;
    private final JsonPointer labelPointer;

    private static final String COLLECTION = "keywords";

    public LocalKeywordVocabulary(
        String vocabularyId,
        String vocabularyName,
        String filePath,
        String resultsPath,
        String uriPath,
        String labelPath,
        SolrClient solrClient,
        List<String> catalogueIds
    ) {
        this.vocabularyId = vocabularyId;
        this.vocabularyName = vocabularyName;
        this.filePath = filePath;
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

        // Delete existing entries for this vocabulary
        solrClient.deleteByQuery(COLLECTION, "vocabId:" + vocabularyId);

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new KeywordVocabularyException("File does not exist: " + filePath);
            }

            // Process and index vocabulary terms from JSON file
            JsonNode rootNode = objectMapper.readTree(file);
            JsonNode vocabularyNode = Optional.ofNullable(rootNode)
                .orElseThrow(() -> new KeywordVocabularyException("Cannot read JSON file"))
                .at(resultsArrayPointer);

            if (vocabularyNode.isArray()) {
                log.info("Retrieved {} terms", vocabularyNode.size());
                StreamSupport.stream(vocabularyNode.spliterator(), false)
                    .map(node -> {
                        JsonNode uriNode = node.at(uriPointer);
                        JsonNode labelNode = node.at(labelPointer);

                        if (uriNode.isMissingNode() || labelNode.isMissingNode()) {
                            return null;
                        }

                        String url = uriNode.asText();
                        String label = labelNode.asText();

                        return new Keyword(label, vocabularyId, url);
                    })
                    .filter(Objects::nonNull)
                    .forEach(keyword -> {
                        try {
                            solrClient.addBean(COLLECTION, keyword);
                        } catch (IOException | SolrServerException ex) {
                            throw new KeywordVocabularyException("Failed to index " + keyword + " for " + vocabularyId, ex);
                        }
                    });
                solrClient.commit(COLLECTION);
            } else {
                throw new KeywordVocabularyException("Expected an array in file: " + filePath);
            }
        } catch (IOException ex) {
            log.error("Failed to process file: {}", filePath, ex);
            throw new KeywordVocabularyException(
                format("Failed to retrieve vocabulary from file %s for %s", filePath, vocabularyId),
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
