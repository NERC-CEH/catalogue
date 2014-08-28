package uk.ac.ceh.gateway.catalogue.gemini;

import java.util.List;

public interface ScienceAreaIndexer {
    /**
     * Extracts Science Area keywords from GeminiDocument in Solr hierarchical format.
     * 
     * The strings need to represent the flattened hierarchy, see http://wiki.apache.org/solr/HierarchicalFaceting
     * 
     * A hierarchy of Soil > Erosion needs to be represented as a list of two strings
     * 
     * "0/Soil/", "1/Soil/Erosion/"
     * 
     * The number prefix represents the depth into the hierarchy.
     * 
     * @param document the GeminiDocument to extract Science Area keywords from
     * @return list of Solr hierarchical facet strings
     */
    List<String> index(GeminiDocument document);
}