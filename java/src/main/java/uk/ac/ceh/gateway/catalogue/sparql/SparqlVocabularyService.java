package uk.ac.ceh.gateway.catalogue.sparql;

import com.google.common.collect.Multimap;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SparqlVocabularyService implements VocabularyService {
    private final Multimap<String, String> vocabulary;

    @Override
    public boolean isMember(String broader, String keyword) {
        return vocabulary.containsEntry(broader, keyword);
    }
}
