package uk.ac.ceh.gateway.catalogue.services;

import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.CatalogueNotFoundException;

public class HardcodedCatalogueService implements CatalogueService {
    private final Map<String, Catalogue> catalogues;

    public HardcodedCatalogueService() {
        
        Catalogue ceh = Catalogue.builder()
            .hostname("catalogue.ceh.ac.uk")
            .title("CEH Catalogue")
            .facetKey("catalogue")
            .facetKey("topic")
            .facetKey("resourceType")
            .facetKey("licence")
            .build();
        
        Catalogue inlicensed = Catalogue.builder()
            .hostname("ceh-in-licensed.catalogue.ceh.ac.uk")
            .title("CEH In-licensed data")
            .facetKey("topic")
            .facetKey("resourceType")
            .facetKey("licence")
            .build();
        
        Catalogue eidc = Catalogue.builder()
            .hostname("eidc.catalogue.ceh.ac.uk")
            .title("Environmental Information Data Centre")
            .facetKey("topic")
            .facetKey("resourceType")
            .facetKey("licence")
            .build();
        
        Catalogue cmp = Catalogue.builder()
            .hostname("cmp.catalogue.ceh.ac.uk")
            .title("Catchment Management Platform")
            .facetKey("impBroaderCatchmentIssues")
            .facetKey("impScale")
            .facetKey("impWaterQuality")
            .facetKey("resourceType")
            .facetKey("licence")
            .build();
        
        Catalogue nfra = Catalogue.builder()
            .hostname("nrfa.catalogue.ceh.ac.uk")
            .title("UK National River Flow Archive data holdings")
            .facetKey("resourceType")
            .facetKey("licence")
            .build();

        catalogues = new HashMap<>();
        catalogues.put("catalogue.ceh.ac.uk", ceh);
        catalogues.put("ceh-in-licensed.catalogue.ceh.ac.uk", inlicensed);
        catalogues.put("eidc.catalogue.ceh.ac.uk", eidc);
        catalogues.put("cmp.catalogue.ceh.ac.uk", cmp);
        catalogues.put("nrfa.catalogue.ceh.ac.uk", nfra);
        
    }

    @Override
    public Catalogue retrieve(@NonNull String hostname) {
        if (catalogues.containsKey(hostname)) {
            return catalogues.get(hostname);
        } else {
            throw new CatalogueNotFoundException(
                String.format("No catalogue for hostname: %s", hostname)
            );
        }
    }

}
