package uk.ac.ceh.gateway.catalogue.search;

import uk.ac.ceh.gateway.catalogue.vocabularies.Keyword;

import java.util.List;

public interface BroaderNarrowerRetriever {
    List<Keyword> retrieve(Keyword keyword);
}
