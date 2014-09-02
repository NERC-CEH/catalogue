package uk.ac.ceh.gateway.catalogue.search;

import java.util.List;
import lombok.Value;
import lombok.experimental.Builder;

@Value
@Builder
public class Facet {
    private final String fieldName;
    private final String displayName;
    private final List<FacetResult> results;
}