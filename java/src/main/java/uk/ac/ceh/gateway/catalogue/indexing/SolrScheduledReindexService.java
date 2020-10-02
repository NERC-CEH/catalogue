package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SolrScheduledReindexService {
    private final DocumentIndexingService solrIndex;

    public SolrScheduledReindexService(
            @Qualifier("solr-index") DocumentIndexingService solrIndex
    ) {
        this.solrIndex = solrIndex;
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
