package uk.ac.ceh.gateway.catalogue.relationships;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Which kinds of record each relationship may point at (dri-one #439).
 * <p>
 * This is the one place the rules are defined. The editor's record picker fetches them, resolved
 * for the record being edited, from {@link RelationshipRulesController}, and
 * {@code GitDocumentRepository} enforces them when a save adds a relationship, because the API
 * and hand-edited JSON do not go through the picker.
 * <p>
 * Types are raw record type keys ({@code MetadataDocument.getType()}), never display labels.
 * <p>
 * A relationship missing from {@link #RULES} is deliberately open: any record may be its target.
 * <ul>
 *   <li>{@code dcterms:relation} and CEHMD {@code rels/related} mean "related", which is
 *       intentionally broad.</li>
 *   <li>CEHMD {@code rels/cites} and {@code rels/uses} (code and infrastructure records) are never
 *       read back by any page or export, so there is no rendering that says what they should
 *       point at. Restrict them once something does.</li>
 * </ul>
 */
public final class RelationshipRules {
    public static final String IS_PART_OF = "http://purl.org/dc/terms/isPartOf";
    public static final String REPLACES = "http://purl.org/dc/terms/replaces";
    public static final String REQUIRES = "http://purl.org/dc/terms/requires";
    public static final String SOURCE = "http://purl.org/dc/terms/source";
    public static final String HAS_OUTPUT = "http://purl.org/cerif/frapo/hasOutput";
    public static final String UTILISES = "https://digital.ceh.ac.uk/ontology/doo/utilises";
    public static final String USES = "https://digital.ceh.ac.uk/ontology/doo/uses";
    public static final String TRIGGERS = "https://digital.ceh.ac.uk/ontology/doo/triggers";
    public static final String HAS_CHILD_FACILITY = "https://digital.ceh.ac.uk/ontology/doo/hasChildFacility";
    public static final String HAS_CHILD_NETWORK = "https://digital.ceh.ac.uk/ontology/doo/hasChildNetwork";
    public static final String HAS_CHILD_PROGRAMME = "https://digital.ceh.ac.uk/ontology/doo/hasChildProgramme";
    public static final String SUPERSEDES = "http://onto.nerc.ac.uk/CEHMD/rels/supersedes";

    public static final String MONITORING_FACILITY = "monitoringFacility";
    public static final String MONITORING_NETWORK = "monitoringNetwork";

    /** Where monitoring data is produced: what {@code doo:utilises} and {@code doo:uses} point at. */
    public static final Set<String> MONITORING_SITES = Set.of(MONITORING_FACILITY, MONITORING_NETWORK);

    /** Records holding data, which services require and code or infrastructure produces. */
    static final Set<String> DATA_RESOURCES = Set.of(
        "dataset", "nonGeographicDataset", "aggregate", "thirdPartyDataset", "signpost", "nercSignpost"
    );

    /** What a research activity's outputs can be (the Model, Dataset, Map and Software record types). */
    static final Set<String> RESEARCH_OUTPUTS = Set.of(
        "dataset", "nonGeographicDataset", "service", "model", "nercModel", "software"
    );

    /**
     * Facilities and networks live in {@code ukceh} (and {@code ukeof}), not where most datasets do,
     * so {@code doo:utilises} searches across catalogues. {@code ukeof} is left out for now
     * (dri-one #439) and may be added later.
     */
    static final List<String> MONITORING_SITE_CATALOGUES = List.of("eidc", "ukceh");

    /**
     * The rule for one relationship.
     *
     * @param targetTypes the record types it may point at, or empty to mean the source's own type
     * @param catalogues the catalogues the picker searches, or empty for the source's own catalogue.
     *                   Only the picker uses this: a save is not checked against catalogues.
     */
    public record Rule(Set<String> targetTypes, List<String> catalogues) {
        static Rule types(Set<String> targetTypes) {
            return new Rule(targetTypes, List.of());
        }

        static Rule sameType() {
            return new Rule(Set.of(), List.of());
        }

        public boolean isSameType() {
            return targetTypes.isEmpty();
        }

        /** The record types this rule allows for a source of the given type. */
        public Set<String> targetTypesFor(String sourceType) {
            if (!isSameType()) {
                return targetTypes;
            }
            return sourceType == null ? Set.of() : Set.of(sourceType);
        }

        public boolean allows(String sourceType, String targetType) {
            return targetType != null && targetTypesFor(sourceType).contains(targetType);
        }
    }

    private static final Map<String, Rule> RULES = Map.ofEntries(
        // A data resource is part of a data collection; a facility is part of a network (below)
        Map.entry(IS_PART_OF, Rule.types(Set.of("aggregate"))),
        Map.entry(REPLACES, Rule.sameType()),
        Map.entry(REQUIRES, Rule.types(DATA_RESOURCES)),
        Map.entry(SOURCE, Rule.types(DATA_RESOURCES)),
        Map.entry(HAS_OUTPUT, Rule.types(RESEARCH_OUTPUTS)),
        Map.entry(UTILISES, new Rule(MONITORING_SITES, MONITORING_SITE_CATALOGUES)),
        Map.entry(USES, Rule.types(MONITORING_SITES)),
        Map.entry(TRIGGERS, Rule.types(Set.of("monitoringActivity"))),
        Map.entry(HAS_CHILD_FACILITY, Rule.sameType()),
        Map.entry(HAS_CHILD_NETWORK, Rule.sameType()),
        Map.entry(HAS_CHILD_PROGRAMME, Rule.sameType()),
        Map.entry(SUPERSEDES, Rule.sameType())
    );

    /** Rules that depend on the kind of record the relationship starts from. */
    private static final Map<String, Map<String, Rule>> SOURCE_RULES = Map.of(
        MONITORING_FACILITY, Map.of(IS_PART_OF, Rule.types(Set.of(MONITORING_NETWORK)))
    );

    private RelationshipRules() {}

    /** The rule for a relationship from a record of {@code sourceType}, or empty if it is open. */
    public static Optional<Rule> ruleFor(String sourceType, String predicate) {
        // Map.of rejects a null key even on lookup, and a record can be saved with no type
        return Optional.ofNullable(sourceType)
            .map(type -> SOURCE_RULES.getOrDefault(type, Map.of()).get(predicate))
            .or(() -> Optional.ofNullable(RULES.get(predicate)));
    }

    /** Every restricted relationship's predicate. */
    public static Set<String> predicates() {
        return RULES.keySet();
    }
}
