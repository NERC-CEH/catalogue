package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.vocab.Concept;
import uk.ac.ceh.components.vocab.Vocabulary;
import uk.ac.ceh.components.vocab.VocabularyException;
import uk.ac.ceh.gateway.catalogue.config.HttpVocabularyConfig;
import uk.ac.ceh.gateway.catalogue.model.SolrVocabularyIndex;

@Slf4j
@Service
public class VocabHarvestingService {
    private final SolrClient solrClient;
    private final Logger logger = LoggerFactory.getLogger(VocabHarvestingService.class);

    public VocabHarvestingService(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    @Scheduled(fixedDelay = 36000000)
    public void harvestVocabularies() throws SolrServerException, IOException, VocabularyException {
        HttpVocabularyConfig config = new HttpVocabularyConfig();
        solrClient.deleteByQuery("type:keyword");
        processVocabularies(config.keywords(), "keyword");
    }

    @Scheduled(fixedDelay = 36000000)
    public void harvestCodelists() throws SolrServerException, IOException, VocabularyException {
        HttpVocabularyConfig config = new HttpVocabularyConfig();
        solrClient.deleteByQuery("type:codelist");
        processVocabularies(config.codelists(), "codelist");
    }

    private void processVocabularies (List<Vocabulary> vocabularies, String type) throws VocabularyException, SolrServerException, IOException {
        for(Vocabulary vocabulary: vocabularies) {
            List<SolrVocabularyIndex> toIndex = transformResponse(vocabulary.getAllConcepts(), vocabulary.getName(), type);
            logger.debug("Vocabulary: {}, type: {} adding with terms: {}", vocabulary.getName(), type, vocabulary.getAllConcepts());
            if (!vocabulary.getAllConcepts().isEmpty()) {
                solrClient.addBeans(toIndex);
                solrClient.commit();
            }
        }
    }

    private List<SolrVocabularyIndex> transformResponse(List<Concept> concepts, String name, String type) {
        List<SolrVocabularyIndex> toReturn = new ArrayList<>();
        for(Concept concept : concepts) {
            log.info("Adding: " + concept.getTerm());
            toReturn.add(new SolrVocabularyIndex()
                                    .setTerm(concept.getTerm())
                                    .setUri(concept.getUri())
                                    .setVocab(name)
                                    .setType(type));
        }
        return toReturn;
    }
}
