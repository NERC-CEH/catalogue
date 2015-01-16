package uk.ac.ceh.gateway.catalogue.indexing;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

public class ExtractTopicFromDocument implements TopicIndexer {
    
    private final Map<String, List<String>> topicHierarchy;
    
    public ExtractTopicFromDocument() {
        topicHierarchy = new HashMap<>();
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/1", ImmutableList.of("0/Agriculture/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/2", ImmutableList.of("0/Climate/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/2_1", ImmutableList.of("0/Climate/", "1/Climate/Climate change/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/2_2", ImmutableList.of("0/Climate/", "1/Climate/Meteorology/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/3", ImmutableList.of("0/Modelling/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/3_1", ImmutableList.of("0/Modelling/", "1/Modelling/Integrated ecosystem modelling/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/4", ImmutableList.of("0/Natural capital/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/4_1", ImmutableList.of("0/Natural capital/", "1/Natural capital/Ecosystem services/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/4_2", ImmutableList.of("0/Natural capital/", "1/Natural capital/Pollinators/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/4_3", ImmutableList.of("0/Natural capital/", "1/Natural capital/Biodiversity/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/5", ImmutableList.of("0/Natural hazards/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/5_1", ImmutableList.of("0/Natural hazards/", "1/Natural hazards/Invasive species/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/5_2", ImmutableList.of("0/Natural hazards/", "1/Natural hazards/Flood/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/5_3", ImmutableList.of("0/Natural hazards/", "1/Natural hazards/Drought/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/6", ImmutableList.of("0/Pollution/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/6_1", ImmutableList.of("0/Pollution/", "1/Pollution/Atmospheric pollution/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/6_2", ImmutableList.of("0/Pollution/", "1/Pollution/Organic pollutants/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/6_3", ImmutableList.of("0/Pollution/", "1/Pollution/Radionuclides/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/7", ImmutableList.of("0/Soil/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/8", ImmutableList.of("0/Hydrology/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/9", ImmutableList.of("0/Water quality/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/10", ImmutableList.of("0/Survey and monitoring/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/10_1", ImmutableList.of("0/Survey and monitoring/", "1/Survey and monitoring/Long term monitoring/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/10_2", ImmutableList.of("0/Survey and monitoring/", "1/Survey and monitoring/Real-time monitoring/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/11", ImmutableList.of("0/Ecosystems and landscapes/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/11_1", ImmutableList.of("0/Ecosystems and landscapes/", "1/Ecosystems and landscapes/Habitat fragmentation/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/11_2", ImmutableList.of("0/Ecosystems and landscapes/", "1/Ecosystems and landscapes/Land cover/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/11_3", ImmutableList.of("0/Ecosystems and landscapes/", "1/Ecosystems and landscapes/Land use/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/12", ImmutableList.of("0/Species distribution/")); 
    }

    @Override
    public List<String> index(MetadataDocument document) {
        
        List<String> topics = Optional.ofNullable(document)
            .map(MetadataDocument::getTopics)
            .orElse(Collections.EMPTY_LIST);
        
       return topics
           .stream()
           .filter(t -> topicHierarchy.containsKey(t))
           .flatMap(t -> topicHierarchy.get(t).stream())
           .distinct()
           .collect(Collectors.toList());
    }
}