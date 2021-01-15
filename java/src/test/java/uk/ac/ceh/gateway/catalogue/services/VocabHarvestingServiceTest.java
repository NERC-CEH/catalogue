package uk.ac.ceh.gateway.catalogue.services;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ceh.components.vocab.Concept;
import uk.ac.ceh.components.vocab.Vocabulary;
import uk.ac.ceh.components.vocab.sparql.SparqlConcept;
import uk.ac.ceh.gateway.catalogue.config.VocabularyConfig;
import uk.ac.ceh.gateway.catalogue.model.SolrVocabularyIndex;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class VocabHarvestingServiceTest {

    @Mock
    private SolrClient solrClient;

    @Test
    @SneakyThrows
    public void harvestVocabulariesSuccessfully() {
        //given
        val config = getVocabularyConfig();
        val concepts = config
            .keywords()
            .get(0)
            .getAllConcepts()
            .stream()
            .map(concept -> new SolrVocabularyIndex(concept, "test", "keyword"))
            .collect(Collectors.toList());

        val service = new VocabHarvestingService(
            solrClient,
            config,
            "vocab"
        );

        //when
        service.harvestVocabularies();

        //then
        verify(solrClient).deleteByQuery("vocab", "type:keyword");
        verify(solrClient).addBeans("vocab", concepts);
    }

    private VocabularyConfig getVocabularyConfig() {
        return new VocabularyConfig() {
            @Override
            public List<Vocabulary> keywords() {
                return Collections.singletonList(
                    new Vocabulary() {
                        @Override
                        public String getUrl() {
                            return "https://example.com/test";
                        }

                        @Override
                        public String getName() {
                            return "test";
                        }

                        @Override
                        public List<Concept> getAllConcepts() {
                            return Arrays.asList(
                                new SparqlConcept().setUri("https://example.com/test-1/concept-1").setTerm("concept-1"),
                                new SparqlConcept().setUri("https://example.com/test-1/concept-2").setTerm("concept-2")
                            );
                        }
                    }
                );
            }

            @Override
            public List<Vocabulary> codelists() {
                return Collections.emptyList();
            }
        };
    }

}