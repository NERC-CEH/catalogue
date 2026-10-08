package uk.ac.ceh.gateway.catalogue.repository.checks;

import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.InvalidRelationshipTargetException;
import uk.ac.ceh.gateway.catalogue.model.LinkDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.PermissionDeniedException;
import uk.ac.ceh.gateway.catalogue.model.Relationship;
import uk.ac.ceh.gateway.catalogue.model.RelationshipTargetCheckException;
import uk.ac.ceh.gateway.catalogue.monitoring.Utilises;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.relationships.RelationshipRules;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Rejects a save that adds a relationship to a kind of record that relationship cannot point
 * at, e.g. a "produced at" ({@code doo:utilises}) link to anything but a monitoring facility
 * or network. {@link RelationshipRules} holds the rules, and the editor's record picker only
 * offers what they allow, but the API and hand-edited JSON bypass the picker (dri-one #404,
 * #439). A relationship with no rule is open and never checked.
 *
 * <p>Like {@link NoNewMojibake}, only relationships this save <em>introduces</em> are
 * checked. One already stored is left alone, so a record holding one stays editable; for
 * {@code doo:utilises} its page and JSON drop it anyway ({@link Utilises#targets}). Checking
 * only new relationships also keeps an ordinary save from reading every target it already
 * points at.
 */
@Slf4j
public class RelationshipTargets implements SaveCheck {
    /** A document id as {@link DocumentIdentifierService#generateFileId} produces it. */
    private static final Pattern DOCUMENT_ID = Pattern.compile("[A-Za-z0-9_-]+");

    private final DocumentIdentifierService documentIdentifierService;
    private final BundledReaderService<MetadataDocument> documentBundleReader;
    private final PermissionService permissionService;

    public RelationshipTargets(
        DocumentIdentifierService documentIdentifierService,
        BundledReaderService<MetadataDocument> documentBundleReader,
        PermissionService permissionService
    ) {
        this.documentIdentifierService = documentIdentifierService;
        this.documentBundleReader = documentBundleReader;
        this.permissionService = permissionService;
    }

    @Override
    public void check(CatalogueUser user, MetadataDocument incoming, String id, StoredVersion stored) {
        Set<Relationship> added = restrictedRelationships(incoming);
        if (added.isEmpty()) {
            return;
        }
        Set<Relationship> existing = stored.get()
            .map(RelationshipTargets::restrictedRelationships)
            .orElseGet(Set::of);
        String sourceType = incoming.getType();
        String invalid = added.stream()
            .filter(relationship -> !existing.contains(relationship))
            .filter(relationship -> !isAllowedTarget(user, sourceType, relationship))
            .map(relationship -> relationship.getTarget() + " (" + relationship.getRelation() + ", which links to " +
                allowedTypes(sourceType, relationship.getRelation()) + ")")
            .sorted()
            .collect(Collectors.joining("; "));
        if (!invalid.isEmpty()) {
            // The same message whether a target is missing, hidden from the user or of the wrong
            // type, so it cannot reveal which (see isAllowedTarget). Raw type keys rather than
            // labels: the editor's picker filters by label, so this mostly reaches API callers.
            throw new InvalidRelationshipTargetException(
                "Document " + id + " links to records that cannot be used for those relationships: " +
                    invalid + ". Each must be a record you can view, of one of the types shown."
            );
        }
    }

    private static String allowedTypes(String sourceType, String predicate) {
        Set<String> types = RelationshipRules.ruleFor(sourceType, predicate).orElseThrow().targetTypesFor(sourceType);
        return types.isEmpty() ? "a record of this document's own type" : String.join(" or ", new TreeSet<>(types));
    }

    /** A document's relationships that have a {@link RelationshipRules} rule for its type. */
    private static Set<Relationship> restrictedRelationships(MetadataDocument document) {
        String sourceType = document.getType();
        return Optional.ofNullable(document.getRelationships()).orElseGet(Set::of).stream()
            .filter(relationship -> RelationshipRules.ruleFor(sourceType, relationship.getRelation()).isPresent())
            .collect(Collectors.toSet());
    }

    /**
     * Whether a relationship's target is a record that the user saving can view, of a type its
     * rule allows. The target need not be in the same catalogue as the document.
     * <p>
     * The view check comes before the target is read, and a record the user cannot see is
     * rejected exactly as a missing one or one of the wrong type is. Otherwise whether a save
     * succeeds would tell anyone who can edit a record whether some draft or private record
     * exists, and roughly what type it is. A datastore fault is different: it says nothing about
     * the target, so it is a {@link RelationshipTargetCheckException} rather than a rejection.
     * <p>
     * A target is either a record URI or a bare record id: the editor's record picker stores the
     * id (Solr's {@code identifier}), and Jena indexing resolves a bare id against the base URI
     * just as it would the URI. Either way the id must have the shape
     * {@link DocumentIdentifierService} generates, so a crafted target cannot steer the read to
     * some other path in the datastore.
     */
    private boolean isAllowedTarget(CatalogueUser user, String sourceType, Relationship relationship) {
        String targetId = documentId(relationship.getTarget());
        if (targetId == null || !canView(user, targetId)) {
            return false;
        }
        String targetType = recordType(targetId);
        return targetType != null && RelationshipRules.ruleFor(sourceType, relationship.getRelation())
            .map(rule -> rule.allows(sourceType, targetType))
            .orElse(true);
    }

    /** The document id a target refers to, or null if it is not one this application generates. */
    private String documentId(String target) {
        if (target == null) {
            return null;
        }
        String prefix = documentIdentifierService.getBaseUri() + "/id/";
        String id = target.startsWith(prefix) ? target.substring(prefix.length()) : target;
        return DOCUMENT_ID.matcher(id).matches() ? id : null;
    }

    /** A record that does not exist is one the user cannot view. */
    private boolean canView(CatalogueUser user, String targetId) {
        try {
            return permissionService.toAccess(user, targetId, "VIEW");
        } catch (PermissionDeniedException ex) {
            if (StoredVersion.isNotFound(ex)) {
                log.debug("Relationship target {} does not exist", targetId);
                return false;
            }
            throw cannotCheck(targetId, ex);
        } catch (RuntimeException ex) {
            throw cannotCheck(targetId, ex);
        }
    }

    /**
     * The type of the record a target id names, or null if there is no such record or it cannot
     * be a relationship target.
     * <p>
     * A link document is never a target. It stands in for a record from another catalogue
     * (UK-SCAPE holds dozens), link documents are being retired, and a relationship belongs on the
     * record it stands in for. Its own type is empty, and the record it names is deliberately not
     * read: anyone who can create records in a catalogue can create a link document to any id, so
     * following it would carry a hidden record's existence and type back through this check.
     */
    private String recordType(String targetId) {
        MetadataDocument target = readTarget(targetId);
        return target == null || target instanceof LinkDocument ? null : target.getType();
    }

    private MetadataDocument readTarget(String id) {
        try {
            return documentBundleReader.readBundle(id);
        } catch (Exception ex) {
            if (StoredVersion.isNotFound(ex)) {
                return null;
            }
            throw cannotCheck(id, ex);
        }
    }

    private RelationshipTargetCheckException cannotCheck(String targetId, Exception cause) {
        log.warn("Cannot check relationship target {}", targetId, cause);
        return new RelationshipTargetCheckException(
            "The records this document links to could not be checked just now. Please try saving again.",
            cause
        );
    }
}
