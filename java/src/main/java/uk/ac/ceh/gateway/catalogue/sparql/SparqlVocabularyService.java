package uk.ac.ceh.gateway.catalogue.sparql;

import com.google.common.collect.Multimap;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString(of = "")
public class SparqlVocabularyService implements VocabularyService {
    private final Multimap<String, String> vocabulary;

    public SparqlVocabularyService(Multimap<String, String> vocabulary) {
        this.vocabulary = vocabulary;
        log.info("Creating {}", this);
    }

    @Override
    public boolean isMember(String broader, String keyword) {
        return vocabulary.containsEntry(broader, keyword);
    }
}
