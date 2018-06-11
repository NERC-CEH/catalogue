package uk.ac.ceh.gateway.catalogue.sparql;

public interface VocabularyService {
    boolean isMember(String broader, String keyword);
}
