package uk.ac.ceh.gateway.catalogue.gemini;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Provides a random list of Science Area strings for Solr faceted search.
 * 
 * This has been produced to demonstrate hierarchical faceted searching without
 * having to work out how to store this information in the metadata record.
 * 
 * @author rjsc
 */
public class CrazyScienceAreaIndexer implements ScienceAreaIndexer {
    private final List<Map<String, String>> categories;
    private final Random random;

    public CrazyScienceAreaIndexer() {
        categories = new ArrayList<>();
        categories.add(make("Biosphere Atmosphere Interactions", null));
        categories.add(make("Biosphere Atmosphere Interactions", "Aerosols"));
        categories.add(make("Biosphere Atmosphere Interactions", "Air Quality"));
        categories.add(make("Ecological Processes & Resilience", null));
        categories.add(make("Ecological Processes & Resilience", "Habitat Loss"));
        categories.add(make("Ecological Processes & Resilience", "Ecosystem Services"));
        categories.add(make("Natural Capital", null));
        categories.add(make("Natural Capital", "Environmental Assets"));
        categories.add(make("Natural Capital", "Insect Pollinators"));
        categories.add(make("Natural Hazards", null));
        categories.add(make("Natural Hazards", "Population Growth"));
        categories.add(make("Natural Hazards", "Mitigation Strategies"));
        categories.add(make("Pollution & Environmental Risk", null));
        categories.add(make("Pollution & Environmental Risk", "Radionuclides"));
        categories.add(make("Pollution & Environmental Risk", "Health"));
        categories.add(make("Soil", null));
        categories.add(make("Soil", "Nutrients"));
        categories.add(make("Soil", "Soil Quality"));
        categories.add(make("Sustainable Land Mangement", null));
        categories.add(make("Sustainable Land Mangement", "Renewable Energy"));
        categories.add(make("Sustainable Land Mangement", "Clean Water"));
        categories.add(make("Water Resources", null));
        categories.add(make("Water Resources", "Agriculture"));
        categories.add(make("Water Resources", "Power Generation"));
        categories.add(make("Monitoring & Observation Systems", null));
        categories.add(make("Monitoring & Observation Systems", "Surveillance"));
        categories.add(make("Monitoring & Observation Systems", "Analysis"));
        random = new Random(System.currentTimeMillis());
    }
    
    private Map<String, String> make (String value0, String value1) {
        Map<String, String> toReturn = new HashMap<>();
        if (value0 != null) {
            toReturn.put("sci0", value0);
        }
        if (value1 != null) {
            toReturn.put("sci1", value1);
        }
        return toReturn;
    }
    

    @Override
    public Map<String, String> index(GeminiDocument document) {
        return categories.get(random.nextInt(categories.size()));
    }

}