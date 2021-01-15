package uk.ac.ceh.gateway.catalogue.services;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.vocab.Concept;
import uk.ac.ceh.components.vocab.Vocabulary;
import uk.ac.ceh.components.vocab.VocabularyException;
import uk.ac.ceh.gateway.catalogue.config.VocabularyConfig;
import uk.ac.ceh.gateway.catalogue.model.SolrVocabularyIndex;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ToString
@Service
public class VocabHarvestingService {
    private final SolrClient solrClient;
    private final VocabularyConfig config;
    private final String collection;

    public VocabHarvestingService(
        SolrClient solrClient,
        VocabularyConfig config,
        @Value("${solr.server.collections.vocabulary}") String collection
    ) {
        this.solrClient = solrClient;
        this.config = config;
        this.collection = collection;
        log.info("Creating {}", collection);
    }

    @Scheduled(fixedDelay = 36000000)
    public void harvestVocabularies() throws SolrServerException, IOException, VocabularyException {
        solrClient.deleteByQuery(collection, "type:keyword");
        processVocabularies(config.keywords(), "keyword");
    }

    @Scheduled(fixedDelay = 36000000)
    public void harvestCodelists() throws SolrServerException, IOException, VocabularyException {
        solrClient.deleteByQuery(collection, "type:codelist");
        processVocabularies(config.codelists(), "codelist");
    }

    private void processVocabularies (List<Vocabulary> vocabularies, String type) throws VocabularyException, SolrServerException, IOException {
        for(Vocabulary vocabulary: vocabularies) {
            val toIndex = transformResponse(vocabulary.getAllConcepts(), vocabulary.getName(), type);
            solrClient.addBeans(collection, toIndex);
        }
    }

    private List<SolrVocabularyIndex> transformResponse(List<Concept> concepts, String vocabName, String vocabType) {
        return concepts.stream()
            .map(concept -> new SolrVocabularyIndex(
                concept,
                vocabName,
                vocabType
            ))
            .collect(Collectors.toList());
    }
}
