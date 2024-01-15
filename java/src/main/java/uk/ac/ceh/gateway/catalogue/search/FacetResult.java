package uk.ac.ceh.gateway.catalogue.search;

import static com.google.common.base.Strings.nullToEmpty;
import java.util.ArrayList;
import java.util.List;
import lombok.Value;
import lombok.Builder;

@Value
public class FacetResult {
    private final String name, url;
    private final boolean active;
    private final long count;
    private final List<FacetResult> subFacetResults;
    
    @Builder
    private FacetResult(String name, String url, boolean active, long count, List<FacetResult> subFacetResults) {
        this.name = nullToEmpty(name);
        this.url = nullToEmpty(url);
        this.active = active;
        this.count = count;
        this.subFacetResults = (subFacetResults != null)? subFacetResults : new ArrayList<>();
    }
}
