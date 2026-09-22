package uk.ac.ceh.gateway.catalogue.controllers;

import uk.ac.ceh.gateway.catalogue.model.MetadataPreconditionRequiredException;

/** Parses the HTTP If-Match precondition header into a bare git revision token. */
public final class IfMatchRevision {
    private IfMatchRevision() {}

    /**
     * Extracts the revision an update is predicated on.
     *
     * <p>The header is mandatory: without it there is nothing to compare against and the save would be an
     * unconditional overwrite, so a missing or blank value is rejected outright. A value of {@code *} is
     * the RFC 9110 wildcard — "any current representation" — which is satisfied simply by the record
     * existing, so it passes the precondition without pinning a revision. Both the strong ({@code "rev"})
     * and weak ({@code W/"rev"}) forms are accepted, since a generic HTTP client may echo back either.</p>
     *
     * @return the unquoted revision, or null for {@code *}, meaning "no revision check"
     * @throws MetadataPreconditionRequiredException if the header is null/blank (→ HTTP 428)
     */
    public static String require(String ifMatch) {
        if (ifMatch == null || ifMatch.isBlank()) {
            throw new MetadataPreconditionRequiredException(
                "An If-Match header carrying the record's current revision is required to update it.");
        }
        String trimmed = ifMatch.trim();
        if (trimmed.equals("*")) {
            return null;
        }
        if (trimmed.startsWith("W/")) {
            trimmed = trimmed.substring(2).trim();
        }
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }
}
