package uk.ac.ceh.gateway.catalogue.monitoring;

import uk.ac.ceh.gateway.catalogue.model.Link;

import java.util.List;

/**
 * {@code doo:utilises} reaches a facility or network from two kinds of record: monitoring
 * programmes that run there, and data resources that were produced there (dri-one #404).
 * Their pages list the two separately, so the inverse links are split on the type of the
 * record they come from.
 */
final class UtilisedBy {
    private static final String PROGRAMME = "monitoringProgramme";

    private UtilisedBy() {}

    static List<Link> programmes(List<Link> utilisedBy) {
        return utilisedBy.stream()
            .filter(link -> PROGRAMME.equals(link.getAssociationType()))
            .toList();
    }

    static List<Link> dataResources(List<Link> utilisedBy) {
        return utilisedBy.stream()
            .filter(link -> !PROGRAMME.equals(link.getAssociationType()))
            .toList();
    }
}
