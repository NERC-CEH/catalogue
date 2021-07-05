package uk.ac.ceh.gateway.catalogue.indexing.solr;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NonNull;

import java.util.List;

public interface WellKnownText {
    @NonNull @JsonIgnore
    List<String> getWKTs();
}
