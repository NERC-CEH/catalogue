package uk.ac.ceh.gateway.catalogue.monitoring;

import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

import java.util.List;
import java.util.Set;

/**
 * {@code doo:utilises} links a monitoring programme that runs at a facility or network, or a
 * data resource produced there, to that facility or network (dri-one #404).
 * <p>
 * The editor only offers facilities and networks as targets, but the API and hand-edited JSON
 * do not go through the editor. {@link #isTarget} is what the save-time check enforces, and
 * {@link #targets} re-applies it to links already in the store.
 * <p>
 * Both directions also drop records that are not published: Jena indexes drafts alongside
 * published records, and these links are served on public pages and in public JSON.
 */
public final class Utilises {
    public static final String PREDICATE = "https://digital.ceh.ac.uk/ontology/doo/utilises";

    private static final String PROGRAMME = "monitoringProgramme";
    private static final Set<String> TARGET_TYPES = Set.of("monitoringFacility", "monitoringNetwork");

    private Utilises() {}

    public static boolean isTarget(MetadataDocument document) {
        return document instanceof MonitoringFacility || document instanceof MonitoringNetwork;
    }

    /** The published facilities and networks a record utilises. */
    public static List<Link> targets(List<Link> utilises) {
        return utilises.stream()
            .filter(Utilises::isPublished)
            .filter(link -> TARGET_TYPES.contains(link.getAssociationType()))
            .toList();
    }

    /** The published records that utilise a facility or network. */
    static List<Link> sources(List<Link> utilisedBy) {
        return utilisedBy.stream()
            .filter(Utilises::isPublished)
            .toList();
    }

    /** The programmes among a facility or network's {@link #sources}. */
    static List<Link> programmes(List<Link> utilisedBy) {
        return utilisedBy.stream()
            .filter(link -> PROGRAMME.equals(link.getAssociationType()))
            .toList();
    }

    /** The data resources produced at a facility or network: every other source. */
    static List<Link> dataResources(List<Link> utilisedBy) {
        return utilisedBy.stream()
            .filter(link -> !PROGRAMME.equals(link.getAssociationType()))
            .toList();
    }

    private static boolean isPublished(Link link) {
        return "published".equalsIgnoreCase(link.getPublicationStatus());
    }
}
