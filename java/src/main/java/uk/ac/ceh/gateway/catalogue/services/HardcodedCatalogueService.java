package uk.ac.ceh.gateway.catalogue.services;

import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;

public class HardcodedCatalogueService implements CatalogueService {
    private final Map<String, Catalogue> catalogues;
    private final Catalogue defaultCatalogue;

    public HardcodedCatalogueService() {
        
        this.defaultCatalogue = Catalogue.builder()
            .id("ceh")
            .title("CEH Catalogue")
            .url("https://eip.ceh.ac.uk")
            .facetKey("topic")
            .facetKey("resourceType")
            .facetKey("licence")
            .build();
        
        Catalogue eidc = Catalogue.builder()
            .id("eidc")
            .title("Environmental Information Data Centre")
            .url("http://eidc.ceh.ac.uk")
            .facetKey("topic")
            .facetKey("resourceType")
            .facetKey("licence")
            .build();
        
        Catalogue cmp = Catalogue.builder()
            .id("cmp")
            .title("Catchment Management Platform")
            .url("https://eip.ceh.ac.uk")
            .facetKey("impBroaderCatchmentIssues")
            .facetKey("impScale")
            .facetKey("impWaterQuality")
            .facetKey("resourceType")
            .facetKey("licence")
            .build();

        catalogues = new HashMap<>();
        catalogues.put("eidc.catalogue.ceh.ac.uk", eidc);
        catalogues.put("cmp.catalogue.ceh.ac.uk", cmp);
        
    }

    @Override
    public Catalogue retrieve(@NonNull String hostname) {
        return catalogues.getOrDefault(hostname, defaultCatalogue);
    }

}
