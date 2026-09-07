package uk.ac.ceh.gateway.catalogue.exports;

import java.util.Optional;

public interface DocumentsToTurtleService {
    Optional<String> getBigTtl(String catalogueId);

    /**
     * Invalidates any prefetched/cached Turtle so the next {@link #getBigTtl(String)} call rebuilds it
     * from the current documents, rather than serving a stale prefetched value. Implementations without
     * a cache have nothing to do here.
     */
    default void refresh() {
    }
}
