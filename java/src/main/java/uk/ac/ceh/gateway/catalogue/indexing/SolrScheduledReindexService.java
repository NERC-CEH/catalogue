package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@AllArgsConstructor
public class SolrScheduledReindexService {
    @Qualifier("solr-index") 
    private final DocumentIndexingService solrIndex;
    
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
