package uk.ac.ceh.gateway.catalogue.indexing.solr;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;

@Slf4j
@ToString
@Service
public class SolrScheduledReindexService {
    private final DocumentIndexingService solrIndex;

    public SolrScheduledReindexService(
            @Qualifier("solr-index") DocumentIndexingService solrIndex
    ) {
        this.solrIndex = solrIndex;
        log.info("Creating {}", this);
    }

    @Scheduled(initialDelay = 180000, fixedDelay = 180000)
    protected void reindex() {
        try {
            if (solrIndex.isIndexEmpty()) {
                log.info("Re-indexing Solr");
                solrIndex.rebuildIndex();
            }
        } catch (DocumentIndexingException ex) {
            log.error(ex.getMessage());
        }
    }
}
