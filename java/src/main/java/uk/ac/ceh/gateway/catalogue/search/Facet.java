package uk.ac.ceh.gateway.catalogue.search;

import static com.google.common.base.Strings.nullToEmpty;
import java.util.ArrayList;
import java.util.List;
import lombok.Value;
import lombok.Builder;

@Value
public class Facet {
    private final String fieldName, displayName;
    private final boolean hierarchical;
    private final List<FacetResult> results;
    
    @Builder
    private Facet(String fieldName, String displayName, boolean hierarchical, List<FacetResult> results) {
        this.fieldName = nullToEmpty(fieldName);
        this.displayName = nullToEmpty(displayName);
        this.hierarchical = hierarchical;
        this.results = (results != null) ? results : new ArrayList<>();
    }
    
}
