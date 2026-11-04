package uk.ac.ceh.gateway.catalogue.monitoring;

import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.relationships.RelationshipRules;

import java.util.List;

/**
 * {@code doo:utilises} links a research activity that runs at a facility or network, or a
 * data resource produced there, to that facility or network (dri-one #404).
 * <p>
 * {@link RelationshipRules} says what a {@code doo:utilises} link may point at, and the save-time
 * check enforces it for new links. {@link #targets} re-applies it to links already in the store,
 * which may predate the check.
 * <p>
 * Both directions also drop records that are not published: Jena indexes drafts alongside
 * published records, and these links are served on public pages and in public JSON.
 */
public final class Utilises {
    public static final String PREDICATE = RelationshipRules.UTILISES;

    private static final String RESEARCH_ACTIVITY = "researchActivity";

    private Utilises() {}

    /** The published facilities and networks a record utilises. */
    public static List<Link> targets(List<Link> utilises) {
        return utilises.stream()
            .filter(Utilises::isPublished)
            .filter(link -> RelationshipRules.MONITORING_SITES.contains(link.getAssociationType()))
            .toList();
    }

    /** The published records that utilise a facility or network. */
    static List<Link> sources(List<Link> utilisedBy) {
        return utilisedBy.stream()
            .filter(Utilises::isPublished)
            .toList();
    }

    /** The research activities among a facility or network's {@link #sources}. */
    static List<Link> researchActivities(List<Link> utilisedBy) {
        return utilisedBy.stream()
            .filter(link -> RESEARCH_ACTIVITY.equals(link.getAssociationType()))
            .toList();
    }

    /** The data resources produced at a facility or network: every other source. */
    static List<Link> dataResources(List<Link> utilisedBy) {
        return utilisedBy.stream()
            .filter(link -> !RESEARCH_ACTIVITY.equals(link.getAssociationType()))
            .toList();
    }

    private static boolean isPublished(Link link) {
        return "published".equalsIgnoreCase(link.getPublicationStatus());
    }
}
