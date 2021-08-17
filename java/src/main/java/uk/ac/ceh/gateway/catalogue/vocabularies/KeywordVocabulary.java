package uk.ac.ceh.gateway.catalogue.vocabularies;

public interface KeywordVocabulary {

    void retrieve();

    String getName();

    String getId();

    boolean usedInCatalogue(String catalogueId);
}
