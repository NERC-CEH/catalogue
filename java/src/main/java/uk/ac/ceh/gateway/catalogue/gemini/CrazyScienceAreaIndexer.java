package uk.ac.ceh.gateway.catalogue.gemini;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private final List<List<String>> categories;
    private final Random random;

    public CrazyScienceAreaIndexer() {
        categories = new ArrayList<>();
        categories.add(Arrays.asList("0/BiosphereAtmosphereInteractions/"));
        categories.add(Arrays.asList("0/BiosphereAtmosphereInteractions/", "1/BiosphereAtmosphereInteractions/Aerosols/"));
        categories.add(Arrays.asList("0/BiosphereAtmosphereInteractions/", "1/BiosphereAtmosphereInteractions/AirQuality/"));
        categories.add(Arrays.asList("0/EcologicalProcessesAndResilience/"));
        categories.add(Arrays.asList("0/EcologicalProcessesAndResilience/", "1/EcologicalProcessesAndResilience/HabitatLoss/"));
        categories.add(Arrays.asList("0/EcologicalProcessesAndResilience/", "1/EcologicalProcessesAndResilience/EcosystemServices/"));
        categories.add(Arrays.asList("0/NaturalCapital/"));
        categories.add(Arrays.asList("0/NaturalCapital/", "1/NaturalCapital/EnvironmentalAssets/"));
        categories.add(Arrays.asList("0/NaturalCapital/", "1/NaturalCapital/InsectPollinators/"));
        categories.add(Arrays.asList("0/NaturalHazards/"));
        categories.add(Arrays.asList("0/NaturalHazards/", "1/NaturalHazards/PopulationGrowth/"));
        categories.add(Arrays.asList("0/NaturalHazards/", "1/NaturalHazards/MitigationStrategies/"));
        categories.add(Arrays.asList("0/PollutionAndEnvironmentalRisk/"));
        categories.add(Arrays.asList("0/PollutionAndEnvironmentalRisk/", "1/PollutionAndEnvironmentalRisk/Radionuclides/"));
        categories.add(Arrays.asList("0/PollutionAndEnvironmentalRisk/", "1/PollutionAndEnvironmentalRisk/Health/"));
        categories.add(Arrays.asList("0/Soil/"));
        categories.add(Arrays.asList("0/Soil/", "1/Soil/Nutrients/"));
        categories.add(Arrays.asList("0/Soil/", "1/Soil/SoilQuality/"));
        categories.add(Arrays.asList("0/SustainableLandMangement/"));
        categories.add(Arrays.asList("0/SustainableLandMangement/", "1/SustainableLandMangement/RenewableEnergy/"));
        categories.add(Arrays.asList("0/SustainableLandMangement/", "1/SustainableLandMangement/CleanWater/"));
        categories.add(Arrays.asList("0/WaterResources/"));
        categories.add(Arrays.asList("0/WaterResources/", "1/WaterResources/Agriculture/"));
        categories.add(Arrays.asList("0/WaterResources/", "1/WaterResources/PowerGeneration/"));
        categories.add(Arrays.asList("0/MonitoringAndObservationSystems/"));
        categories.add(Arrays.asList("0/MonitoringAndObservationSystems/", "1/MonitoringAndObservationSystems/Surveillance/"));
        categories.add(Arrays.asList("0/MonitoringAndObservationSystems/", "1/MonitoringAndObservationSystems/Analysis/"));
        random = new Random(System.currentTimeMillis());
    }
    

    @Override
    public List<String> index(GeminiDocument document) {
        return categories.get(random.nextInt(categories.size()));
    }

}