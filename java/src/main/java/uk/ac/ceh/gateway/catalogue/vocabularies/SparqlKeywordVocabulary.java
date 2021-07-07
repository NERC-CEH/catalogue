package uk.ac.ceh.gateway.catalogue.vocabularies;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Profile("elter")
@Slf4j
@ToString(of="address")
public class SparqlKeywordVocabulary implements KeywordVocabulary {

    private String vocabularyId;
    private List<String> catalogueIds;
    private final RestTemplate restTemplate;
    private final SolrClient solrClient;
    private final String address;
    private static final String KEYWORDS = "keywords";

    public SparqlKeywordVocabulary(
            @Qualifier("normal") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${vocabularies.sparql}") String address,
            String vocabularyId,
            List<String> catalogueIds
    ) {
        this.restTemplate = restTemplate;
        this.solrClient = solrClient;
        this.address = address;
        this.vocabularyId = vocabularyId;
        this.catalogueIds = catalogueIds;
        log.info("Creating {}", this);
    }

    public void retrieveVocabularies() throws DocumentIndexingException, AuthenticationException {
        log.info("Re-indexing Vocabularies");
        val response = restTemplate.getForEntity(
                this.address,
                JsonNode.class
        );

        log.debug(response.toString());
        val vocabularyNode = Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new AuthenticationException("Cannot get response body"))
                .at("/results/bindings");

        List<VocabularySolrIndex> results = new ArrayList<>();
        if (vocabularyNode.isArray()) {

            results = StreamSupport.stream(vocabularyNode.spliterator(), false)
                    .map(node -> {
                                val uri = node.get("uri").asText();
                                val label = node.get("label").asText();
                                return new VocabularySolrIndex(uri, label, vocabularyId);
                            }).collect(Collectors.toList());
        }
        try {
            solrClient.deleteByQuery(KEYWORDS, "*:*");
            for (VocabularySolrIndex vocabulary : results) {
                solrClient.addBean(KEYWORDS, vocabulary);
                log.debug("Added {}, {}", vocabulary.getVocabId(), vocabulary.getLabel());
            }
            solrClient.commit(KEYWORDS);

        } catch (IOException | SolrServerException ex) {
            log.error("Failed to re-index Vocabularies");
            throw new DocumentIndexingException(ex);
        }
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public List<String> getCatalogues() {
        return catalogueIds;
    }
}
