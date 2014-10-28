package uk.ac.ceh.gateway.catalogue.indexing;

import com.google.common.collect.Multimap;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

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
     * @return Multimap of Solr column name to Topics
     */
    Multimap<String, String> index(MetadataDocument document);
}