package uk.ac.ceh.gateway.catalogue.serviceagreement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The file listing of a service agreement as it was last agreed: what the Hubbub upload page checks
 * uploads against, even after the agreement has been sent back to draft and edited.
 *
 * @param files      the named files
 * @param fileGroups the groups of files described by a pattern
 * @param fileNumber the depositor's free-text total number of files
 * @param revising   whether the agreement has since left the agreed state, so a newer draft exists. Not
 *                   stored: it is worked out from the agreement's state when the snapshot is read.
 */
public record AgreedFiles(
    List<File> files,
    List<FileGroup> fileGroups,
    String fileNumber,
    @JsonIgnore boolean revising
) {
    /**
     * Reads the stored form, which has no {@code revising}. Without this the canonical constructor is the
     * creator, and Jackson 3 refuses to default its missing primitive.
     */
    @JsonCreator
    static AgreedFiles stored(
        @JsonProperty("files") List<File> files,
        @JsonProperty("fileGroups") List<FileGroup> fileGroups,
        @JsonProperty("fileNumber") String fileNumber
    ) {
        return new AgreedFiles(files, fileGroups, fileNumber, false);
    }

    static AgreedFiles of(ServiceAgreement serviceAgreement, boolean revising) {
        return new AgreedFiles(
            serviceAgreement.getFiles(),
            serviceAgreement.getFileGroups(),
            serviceAgreement.getFileNumber(),
            revising
        );
    }

    AgreedFiles withRevising(boolean revising) {
        return new AgreedFiles(files, fileGroups, fileNumber, revising);
    }
}
