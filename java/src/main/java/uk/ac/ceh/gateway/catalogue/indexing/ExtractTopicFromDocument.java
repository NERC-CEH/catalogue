package uk.ac.ceh.gateway.catalogue.indexing;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.Map;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

public class ExtractTopicFromDocument implements TopicIndexer {
    
    private final Map<String, Map<String, String>> topicHierarchy;
    
    public ExtractTopicFromDocument() {
        topicHierarchy = new HashMap<>();
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/1", ImmutableMap.of("sci0", "Agriculture"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/2", ImmutableMap.of("sci0", "Climate"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/2_1", ImmutableMap.of("sci0", "Climate", "sci1", "Climate change"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/2_2", ImmutableMap.of("sci0", "Climate", "sci1", "Meteorology"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/3", ImmutableMap.of("sci0", "Modelling"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/3_1", ImmutableMap.of("sci0", "Modelling", "sci1", "Integrated ecosystem modelling"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/4", ImmutableMap.of("sci0", "Natural capital"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/4_1", ImmutableMap.of("sci0", "Natural capital", "sci1", "Ecosystem services"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/4_2", ImmutableMap.of("sci0", "Natural capital", "sci1", "Pollinators"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/4_3", ImmutableMap.of("sci0", "Natural capital", "sci1", "Biodiversity"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/5", ImmutableMap.of("sci0", "Natural hazards"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/5_1", ImmutableMap.of("sci0", "Natural hazards", "sci1", "Invasive species"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/5_2", ImmutableMap.of("sci0", "Natural hazards", "sci1", "Flood"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/5_3", ImmutableMap.of("sci0", "Natural hazards", "sci1", "Drought"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/6", ImmutableMap.of("sci0", "Pollution"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/6_1", ImmutableMap.of("sci0", "Pollution", "sci1", "Atmospheric pollution"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/6_2", ImmutableMap.of("sci0", "Pollution", "sci1", "Organic pollutants"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/6_3", ImmutableMap.of("sci0", "Pollution", "sci1", "Radionuclides"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/7", ImmutableMap.of("sci0", "Soil"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/8", ImmutableMap.of("sci0", "Hydrology"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/9", ImmutableMap.of("sci0", "Water quality"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/10", ImmutableMap.of("sci0", "Survey and monitoring"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/10_1", ImmutableMap.of("sci0", "Survey and monitoring", "sci1", "Long term monitoring"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/10_2", ImmutableMap.of("sci0", "Survey and monitoring", "sci1", "Real-time monitoring"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/11", ImmutableMap.of("sci0", "Ecosystems and landscapes"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/11_1", ImmutableMap.of("sci0", "Ecosystems and landscapes", "sci1", "Habitat fragmentation"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/11_2", ImmutableMap.of("sci0", "Ecosystems and landscapes", "sci1", "Land cover"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/11_3", ImmutableMap.of("sci0", "Ecosystems and landscapes", "sci1", "Land use"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/12", ImmutableMap.of("sci0", "Species distribution"));
        
        
    }

    @Override
    public Multimap<String, String> index(MetadataDocument document) {
        Multimap<String, String> toReturn = HashMultimap.create();
        
        document.getTopics().forEach(topic -> {
            if (topicHierarchy.containsKey(topic)) {
                Map<String, String> facetMap = topicHierarchy.get(topic);
                facetMap.keySet().forEach(key -> {
                    toReturn.put(key, facetMap.get(key));
                });  
            }
        });
        
        return toReturn;
    }

}