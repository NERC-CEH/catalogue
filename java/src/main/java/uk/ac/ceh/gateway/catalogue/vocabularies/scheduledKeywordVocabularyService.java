package uk.ac.ceh.gateway.catalogue.vocabularies;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;

import java.io.IOException;

@Profile("elter")
@Slf4j
@ToString(of="address")
@Service
public class scheduledKeywordVocabularyService {

    private final RestTemplate restTemplate;
    private final SolrClient solrClient;
    private final String address;
    private final int ONE_MINUTE = 60000;
    private final int SEVEN_DAYS = 604800000;
    private static final String VOLCABULARIES = "volcabularies";

    public scheduledKeywordVocabularyService(
            @Qualifier("normal") RestTemplate restTemplate,
            SolrClient solrClient,
            @Value("${vocabularies.sparql}") String address
    ) {
        this.restTemplate = restTemplate;
        this.solrClient = solrClient;
        this.address = address;
        log.info("Creating {}", this);
    }

    @Scheduled(initialDelay = ONE_MINUTE, fixedDelay = SEVEN_DAYS)
    public void fetchVocabulariesAndAddToSolr() throws DocumentIndexingException {
        log.info("Re-indexing Vocabularies");
        val response = restTemplate.getForEntity(
                this.address,
                KeywordVocabulary[].class
        );

        KeywordVocabulary[] vocabularies = response.getBody();
        log.info("Retrieved {} Vocabularies", vocabularies.length);
        try {

            solrClient.deleteByQuery(VOLCABULARIES, "*:*");
            for (KeywordVocabulary vocabulary : vocabularies) {
                solrClient.addBean(VOLCABULARIES, new KeywordSolrIndex(vocabulary));
                log.debug("Added {}, {}", vocabulary.getIdentifier(), vocabulary.getLabel());
            }
            solrClient.commit(VOLCABULARIES);

        } catch (IOException | SolrServerException ex) {
            log.error("Failed to re-index Vocabularies");
            throw new DocumentIndexingException(ex);
        }
    }
}
