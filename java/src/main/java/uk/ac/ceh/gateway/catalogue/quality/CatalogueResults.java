package uk.ac.ceh.gateway.catalogue.quality;

import lombok.Value;

import java.util.List;

@Value
public class CatalogueResults {
    List<Results> results;

    public long getTotalErrors() {
        return results.stream()
                .mapToLong(Results::getErrors)
                .sum();
    }

    public long getTotalWarnings() {
        return results.stream()
                .mapToLong(Results::getWarnings)
                .sum();
    }

    public long getTotalInfo() {
        return results.stream()
                .mapToLong(Results::getInfo)
                .sum();
    }
}
