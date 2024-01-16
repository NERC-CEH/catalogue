package uk.ac.ceh.gateway.catalogue.services;

import java.util.Optional;

public interface DocumentsToTurtleService {
    Optional<String> getBigTtl(String catalogueId);
}
