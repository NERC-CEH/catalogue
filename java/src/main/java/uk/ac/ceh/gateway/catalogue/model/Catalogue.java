package uk.ac.ceh.gateway.catalogue.model;

import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import uk.ac.ceh.gateway.catalogue.search.Facet;

@Value
@Builder
public class Catalogue {
    @NonNull private final String key;
    @NonNull private final String title;
    @Singular private final List<Facet> facets;  
}
