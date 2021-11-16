package uk.ac.ceh.gateway.catalogue.vocabularies;

public interface KeywordVocabulary {

    void retrieve();
    String getName();
    String getId();
    String getGraph();
    boolean usedInCatalogue(String catalogueId);
}
