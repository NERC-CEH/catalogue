package uk.ac.ceh.gateway.catalogue.controllers;

import uk.ac.ceh.gateway.catalogue.model.MetadataPreconditionRequiredException;

/** Parses the HTTP If-Match precondition header into a bare git revision token. */
public final class IfMatchRevision {
    private IfMatchRevision() {}

    /**
     * @return the unquoted revision from a non-blank If-Match header
     * @throws MetadataPreconditionRequiredException if the header is null/blank (→ HTTP 428)
     */
    public static String require(String ifMatch) {
        if (ifMatch == null || ifMatch.isBlank()) {
            throw new MetadataPreconditionRequiredException(
                "An If-Match header carrying the record's current revision is required to update it.");
        }
        String trimmed = ifMatch.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }
}
