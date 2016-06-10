package uk.ac.ceh.gateway.catalogue.search;

public interface FacetFactory {
    Facet newInstance(String key);
}
