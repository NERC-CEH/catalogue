package uk.ac.ceh.gateway.catalogue.search;

import java.util.List;

public interface FacetFactory {
    Facet newInstance(String key);
    List<Facet> newInstances(List<String> facetKeys);
}
