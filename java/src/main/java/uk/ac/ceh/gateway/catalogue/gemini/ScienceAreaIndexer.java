package uk.ac.ceh.gateway.catalogue.gemini;

import java.util.Map;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

public interface ScienceAreaIndexer {
    /**
     * Extracts Science Area keywords from GeminiDocument for Solr facet pivot.
     * 
     * The keywords need to represent the flattened hierarchy, see http://wiki.apache.org/solr/HierarchicalFaceting
     * 
     * A hierarchy of Soil > Erosion needs to be represented as a list of two strings
     * 
     * "0/Soil/", "1/Soil/Erosion/"
     * 
     * The number prefix represents the depth into the hierarchy.
     * 
     * @param document the GeminiDocument to extract Science Area keywords from
     * @return map of Solr column name to Science Area
     */
    Map<String, String> index(MetadataDocument document);
}