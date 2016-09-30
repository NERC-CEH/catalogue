package uk.ac.ceh.gateway.catalogue.model;

import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class Catalogue implements Comparable<Catalogue> {
    @NonNull private final String id, title, url;
    @Singular private final List<String> facetKeys;

    @Override
    public int compareTo(Catalogue that) {
        return this.title.compareTo(that.title);
    }
}
