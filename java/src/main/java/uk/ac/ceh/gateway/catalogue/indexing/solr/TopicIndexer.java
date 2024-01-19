package uk.ac.ceh.gateway.catalogue.indexing.solr;

import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;

import java.util.List;

public interface TopicIndexer {
    /**
     * Extracts Topic keywords from GeminiDocument for Solr facet pivot.
     *
     * The keywords need to represent the flattened hierarchy, see http://wiki.apache.org/solr/HierarchicalFaceting
     *
     *
     * The number prefix represents the depth into the hierarchy.
     *
     * @param document the GeminiDocument to extract Science Area keywords from
     * @return List of Topics
     */
    List<String> index(GeminiDocument document);
}
