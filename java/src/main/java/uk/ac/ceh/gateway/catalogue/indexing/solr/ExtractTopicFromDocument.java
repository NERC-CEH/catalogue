package uk.ac.ceh.gateway.catalogue.indexing.solr;

import com.google.common.collect.ImmutableList;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExtractTopicFromDocument implements TopicIndexer {

    private final Map<String, List<String>> topicHierarchy;

    public ExtractTopicFromDocument() {
        topicHierarchy = new HashMap<>();
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/topic/1",  ImmutableList.of("0/Agriculture/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/topic/20", ImmutableList.of("0/Animal behaviour/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/topic/2",  ImmutableList.of("0/Biodiversity/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/topic/4",  ImmutableList.of("0/Climate and climate change/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/topic/6",  ImmutableList.of("0/Ecosystem services/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/topic/5",  ImmutableList.of("0/Environmental risk/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/topic/7",  ImmutableList.of("0/Environmental survey/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/topic/19", ImmutableList.of("0/Evolutionary ecology/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/topic/9",  ImmutableList.of("0/Hydrology/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/topic/10", ImmutableList.of("0/Invasive species/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/topic/11", ImmutableList.of("0/Land cover/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/topic/12", ImmutableList.of("0/Land use/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/topic/18", ImmutableList.of("0/Mapping/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/topic/13", ImmutableList.of("0/Modelling/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/topic/3",  ImmutableList.of("0/Phenology/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/topic/14", ImmutableList.of("0/Pollinators/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/topic/15", ImmutableList.of("0/Pollution/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/topic/8", ImmutableList.of("0/Radioecology/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/topic/17", ImmutableList.of("0/Soil/"));
        topicHierarchy.put("http://onto.nerc.ac.uk/CEHMD/topic/16", ImmutableList.of("0/Water quality/"));
    }

    @Override
    public List<String> index(GeminiDocument document) {
        return document.getTopics()
            .stream()
            .filter(t -> topicHierarchy.containsKey(t))
            .flatMap(t -> topicHierarchy.get(t).stream())
            .distinct()
            .collect(Collectors.toList());
    }
}
