package uk.ac.ceh.gateway.catalogue.exports;

import java.util.Optional;

public interface DocumentsToTurtleService {
    Optional<String> getBigTtl(String catalogueId);
}
