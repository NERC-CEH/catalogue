package uk.ac.ceh.gateway.catalogue.services;

import java.util.HashMap;
import java.util.Map;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.search.Facet;

public class HardcodedCatalogueService implements CatalogueService {
    
    private final Map<String, Catalogue> catalogues;
    private final Catalogue defaultCatalogue;

    public HardcodedCatalogueService() {
        
        Facet resourceType = Facet.builder()
            .fieldName("resourceType")
            .displayName("Resource type")
            .hierarchical(false)
            .build();
        
        Facet licence = Facet.builder()
            .fieldName("licence")
            .displayName("Licence")
            .hierarchical(false)
            .build();
        
        Facet topic = Facet.builder()
            .fieldName("topic")
            .displayName("Topic")
            .hierarchical(true)
            .build();
        
        Facet broader = Facet.builder()
            .fieldName("impBroaderCatchmentIssues")
            .displayName("Broader Catchment Issues")
            .hierarchical(false)
            .build();
        
        Facet scale = Facet.builder()
            .fieldName("impScale")
            .displayName("Scale")
            .hierarchical(false)
            .build();
        
        Facet quality = Facet.builder()
            .fieldName("impWaterQuality")
            .displayName("Water Quality")
            .hierarchical(false)
            .build();
        
        Catalogue ceh = Catalogue.builder()
            .key("ceh")
            .title("CEH Catalogue")
            .facet(resourceType)
            .facet(licence)
            .build();
        
        Catalogue inlicensed = Catalogue.builder()
            .key("ceh-in-licenced")
            .title("CEH In-licensed data")
            .facet(resourceType)
            .facet(licence)
            .build();
        
        Catalogue eidc = Catalogue.builder()
            .key("eidc")
            .title("Environmental Information Data Centre")
            .facet(topic)
            .facet(resourceType)
            .facet(licence)
            .build();
        
        Catalogue cmp = Catalogue.builder()
            .key("cmp")
            .title("Catchment Management Platform")
            .facet(broader)
            .facet(scale)
            .facet(quality)
            .facet(resourceType)
            .facet(licence)
            .build();
        
        Catalogue nfra = Catalogue.builder()
            .key("nrfa")
            .title("UK National River Flow Archive data holdings")
            .facet(resourceType)
            .facet(licence)
            .build();

        catalogues = new HashMap<>();
        catalogues.put("ceh", ceh);
        catalogues.put("inlicensed", inlicensed);
        catalogues.put("eidc", eidc);
        catalogues.put("cmp", cmp);
        catalogues.put("nrfa", nfra);
        
        this.defaultCatalogue = ceh;
        
    }

    @Override
    public Catalogue retrieve(String key) {
        return catalogues.getOrDefault(key, defaultCatalogue);
    }

}
