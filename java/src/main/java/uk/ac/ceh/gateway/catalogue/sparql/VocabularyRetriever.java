package uk.ac.ceh.gateway.catalogue.sparql;

import com.google.common.collect.Multimap;

public interface VocabularyRetriever {
    Multimap<String, String> retrieve();
}
