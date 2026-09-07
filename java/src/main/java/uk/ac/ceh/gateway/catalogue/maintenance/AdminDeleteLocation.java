package uk.ac.ceh.gateway.catalogue.maintenance;

import lombok.Getter;

/**
 * Where in the datastore a record lives. Records sit under one of a small number of prefixes, and the
 * admin delete form needs to reach all of them — including the legacy {@code service-agreements/}
 * directory, which the application itself no longer reads.
 *
 * <p>This is an enum rather than a free-text path on purpose: the prefix is chosen from a fixed set and
 * composed server-side, so a request cannot walk out of the datastore with {@code ../}.</p>
 */
@Getter
public enum AdminDeleteLocation {

    METADATA_RECORD("Metadata record", ""),
    SERVICE_AGREEMENT("Service agreement", "service-agreement/"),
    /**
     * A directory left by an earlier layout. {@code GitRepoServiceAgreementService.FOLDER} is the
     * singular {@code service-agreement/}, so records here are invisible to the application and can
     * only be reached to be removed.
     */
    SERVICE_AGREEMENT_LEGACY("Service agreement (legacy directory)", "service-agreements/");

    private final String label;
    private final String prefix;

    AdminDeleteLocation(String label, String prefix) {
        this.label = label;
        this.prefix = prefix;
    }

    /** The datastore path for {@code id} in this location, without a file extension. */
    public String pathFor(String id) {
        return prefix + id;
    }
}
