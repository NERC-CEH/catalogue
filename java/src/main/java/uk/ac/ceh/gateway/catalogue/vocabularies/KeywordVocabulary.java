package uk.ac.ceh.gateway.catalogue.vocabularies;

import java.util.List;

public interface KeywordVocabulary {

    void retrieve();

    String getName();

    List<String> getCatalogues();
}
