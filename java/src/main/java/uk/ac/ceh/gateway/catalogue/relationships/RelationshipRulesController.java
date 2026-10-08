package uk.ac.ceh.gateway.catalogue.relationships;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ceh.gateway.catalogue.templateHelpers.CodeLookupService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Serves {@link RelationshipRules} to the editor's record picker, resolved for the record being
 * edited, so the picker and the save-time check work from the same rules.
 */
@Hidden
@Slf4j
@ToString
@RestController
public class RelationshipRulesController {
    private final CodeLookupService codeLookupService;

    public RelationshipRulesController(CodeLookupService codeLookupService) {
        this.codeLookupService = codeLookupService;
        log.info("Creating");
    }

    /**
     * What the picker should search for one relationship.
     *
     * @param resourceTypes the target types as Solr indexes them in {@code resourceType}: the
     *                      codelist display label, which is case-sensitive and not the type key
     * @param catalogues the catalogues to search, or empty for the record's own catalogue
     */
    public record Targets(List<String> resourceTypes, List<String> catalogues) {}

    /**
     * The restricted relationships from a record of the given type, keyed by predicate. A
     * relationship that is not listed is open, and the picker searches every record.
     */
    @GetMapping("relationships/rules")
    public Map<String, Targets> rules(@RequestParam("type") String type) {
        Map<String, Targets> rules = new TreeMap<>();
        RelationshipRules.predicates().forEach(predicate ->
            RelationshipRules.ruleFor(type, predicate).ifPresent(rule ->
                rules.put(predicate, new Targets(labels(rule.targetTypesFor(type)), rule.catalogues()))
            )
        );
        return rules;
    }

    private List<String> labels(Set<String> types) {
        return types.stream()
            .map(type -> codeLookupService.lookup("metadata.resourceType", type))
            .filter(Objects::nonNull)
            .distinct()
            .sorted()
            .toList();
    }
}
