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
 * @param hasNamingConvention whether the agreement also has a prose naming convention (kept from before
 *                   file groups), which may describe files not listed, so uploads that match nothing
 *                   cannot be called unexpected. Only whether there is one: the page doesn't show it.
 * @param revising   whether the agreement has since left the agreed state, so a newer draft exists. Not
 *                   stored: it is worked out from the agreement's state when the snapshot is read.
 */
public record AgreedFiles(
    List<File> files,
    List<FileGroup> fileGroups,
    String fileNumber,
    boolean hasNamingConvention,
    @JsonIgnore boolean revising
) {
    /**
     * Reads the stored form, which has no {@code revising}, and no {@code hasNamingConvention} if stored
     * before it was kept. Without this the canonical constructor is the creator, and Jackson 3 refuses to
     * default its missing primitives.
     */
    @JsonCreator
    static AgreedFiles stored(
        @JsonProperty("files") List<File> files,
        @JsonProperty("fileGroups") List<FileGroup> fileGroups,
        @JsonProperty("fileNumber") String fileNumber,
        @JsonProperty("hasNamingConvention") Boolean hasNamingConvention
    ) {
        return new AgreedFiles(files, fileGroups, fileNumber, Boolean.TRUE.equals(hasNamingConvention), false);
    }

    static AgreedFiles of(ServiceAgreement serviceAgreement, boolean revising) {
        return new AgreedFiles(
            serviceAgreement.getFiles(),
            serviceAgreement.getFileGroups(),
            serviceAgreement.getFileNumber(),
            serviceAgreement.getFileNamingConvention() != null && !serviceAgreement.getFileNamingConvention().isBlank(),
            revising
        );
    }

    AgreedFiles withRevising(boolean revising) {
        return new AgreedFiles(files, fileGroups, fileNumber, hasNamingConvention, revising);
    }
}
