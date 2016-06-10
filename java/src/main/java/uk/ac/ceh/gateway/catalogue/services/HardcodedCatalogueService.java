package uk.ac.ceh.gateway.catalogue.services;

import java.util.HashMap;
import java.util.Map;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.search.FacetFactory;

public class HardcodedCatalogueService implements CatalogueService {
    private final FacetFactory facetFactory;
    private final Map<String, Catalogue> catalogues;
    private final Catalogue defaultCatalogue;

    public HardcodedCatalogueService(FacetFactory facetFactory) {
        this.facetFactory = facetFactory;
        
        Catalogue ceh = Catalogue.builder()
            .key("")
            .title("CEH Catalogue")
            .facetKey("topic")
            .facetKey("resourceType")
            .facetKey("licence")
            .build();
        
        Catalogue inlicensed = Catalogue.builder()
            .key("ceh-in-licensed")
            .title("CEH In-licensed data")
            .facetKey("topic")
            .facetKey("resourceType")
            .facetKey("licence")
            .build();
        
        Catalogue eidc = Catalogue.builder()
            .key("eidc")
            .title("Environmental Information Data Centre")
            .facetKey("topic")
            .facetKey("resourceType")
            .facetKey("licence")
            .build();
        
        Catalogue cmp = Catalogue.builder()
            .key("cmp")
            .title("Catchment Management Platform")
            .facetKey("impBroaderCatchmentIssues")
            .facetKey("impScale")
            .facetKey("impWaterQuality")
            .facetKey("resourceType")
            .facetKey("licence")
            .build();
        
        Catalogue nfra = Catalogue.builder()
            .key("nrfa")
            .title("UK National River Flow Archive data holdings")
            .facetKey("resourceType")
            .facetKey("licence")
            .build();

        catalogues = new HashMap<>();
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
