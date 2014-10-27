package uk.ac.ceh.gateway.catalogue.search;

import java.util.List;
import lombok.Value;
import lombok.experimental.Builder;

@Value
@Builder
public class FacetResult {
    private final String name, url;
    private final boolean active;
    private final long count;
    private final List<FacetResult> subFacetResults;
}