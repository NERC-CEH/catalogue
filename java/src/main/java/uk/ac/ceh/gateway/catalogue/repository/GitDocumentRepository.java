package uk.ac.ceh.gateway.catalogue.repository;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.datastore.git.GitFileNotFoundException;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.document.UnknownContentTypeException;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.document.writing.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.monitoring.Utilises;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.relationships.RelationshipRules;
import uk.ac.ceh.gateway.catalogue.services.ResourceIdentifierLookupService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@ToString(onlyExplicitlyIncluded = true)
public class GitDocumentRepository implements DocumentRepository {
    /**
     * Telltale signature of double-encoded ("mojibake") text - UTF-8 bytes decoded as
     * CP1252/Latin-1 and re-encoded as UTF-8. Guards against any ingest path (known or not yet
     * found) baking further corrupted literals into the store.
     *
     * <p>dri-one #328's verification query uses {@code "â€|Â[^ ]"}; the second alternative is
     * narrowed to a non-letter here. Real mojibake is a {@code Â} standing in for a byte that
     * decodes as punctuation or a symbol - {@code Â°}, {@code Â£}, {@code Â©}, {@code Â} plus a
     * non-breaking space - whereas {@code Â} followed by a letter is ordinary text in several
     * languages: Vietnamese {@code Ân}, upper-cased Romanian {@code CÂMPINA}, Welsh {@code TÂN}.
     * Any of those is plausible in a name, title or place keyword, and would otherwise be
     * rejected at save with no override.
     */
    private static final Pattern MOJIBAKE_PATTERN = Pattern.compile("â€|Â[^ A-Za-z]");

    /** A document id as {@link DocumentIdentifierService#generateFileId} produces it. */
    private static final Pattern DOCUMENT_ID = Pattern.compile("[A-Za-z0-9_-]+");

    private final DocumentTypeLookupService documentTypeLookupService;
    private final DocumentReadingService documentReader;
    private final DocumentIdentifierService documentIdentifierService;
    private final DocumentWritingService documentWriter;
    private final BundledReaderService<MetadataDocument> documentBundleReader;
    private final ResourceIdentifierLookupService resourceIdentifierLookupService;
    private final GitRepoWrapper repo;
    private final JsonMapper objectMapper;
    private final PermissionService permissionService;

    public GitDocumentRepository(
        DocumentTypeLookupService documentTypeLookupService,
        DocumentReadingService documentReader,
        DocumentIdentifierService documentIdentifierService,
        DocumentWritingService documentWriter,
        BundledReaderService<MetadataDocument> documentBundleReader,
        ResourceIdentifierLookupService resourceIdentifierLookupService,
        GitRepoWrapper repo,
        JsonMapper objectMapper,
        PermissionService permissionService
    ) {
        this.documentTypeLookupService = documentTypeLookupService;
        this.documentReader = documentReader;
        this.documentIdentifierService = documentIdentifierService;
        this.documentWriter = documentWriter;
        this.documentBundleReader = documentBundleReader;
        this.resourceIdentifierLookupService = resourceIdentifierLookupService;
        this.repo = repo;
        this.objectMapper = objectMapper;
        this.permissionService = permissionService;
        log.info("Creating");
    }

    @Override
    public MetadataDocument read(
        String file
    ) throws DocumentRepositoryException {
        try {
            MetadataDocument document = documentBundleReader.readBundle(file);
            if (document instanceof LinkDocument d) {
                d.setOriginal(
                    documentBundleReader.readBundle(
                        d.getLinkedDocumentId()
                    )
                );
            }
            if (document != null) document.validate();
            return document;
        } catch (IOException | UnknownContentTypeException | PostProcessingException ex) {
            throw new DocumentRepositoryException(
                String.format("Cannot read file: %s", file),
                ex
            );
        }
    }

    @Override
    public MetadataDocument read(
        String file,
        String revision
    ) throws DocumentRepositoryException {
        try {
            MetadataDocument document = documentBundleReader.readBundle(file, revision);

            if (document instanceof LinkDocument d) {
                d.setOriginal(
                    documentBundleReader.readBundle(
                        d.getLinkedDocumentId(),
                        revision
                    )
                );
            }
            if (document != null) document.validate();
            return document;
        } catch (IOException | PostProcessingException | UnknownContentTypeException ex) {
            throw new DocumentRepositoryException(
                String.format("Cannot read file: %s at revision: %s", file, revision),
                ex
            );
        }
    }

    @Override
    public MetadataDocument save(
        CatalogueUser user,
        InputStream inputStream,
        MediaType mediaType,
        String documentType,
        String catalogue,
        String message
    ) throws DocumentRepositoryException {
        try {
            Path tmpFile = Files.createTempFile("upload", null); //Create a temp file to upload the input stream to
            String id;
            MetadataDocument data;
            Class<? extends MetadataDocument> metadataType = documentTypeLookupService.getType(documentType);

            try {
                Files.copy(inputStream, tmpFile, StandardCopyOption.REPLACE_EXISTING); //copy the file so that we can pass over multiple times

                //the documentReader will close the underlying input stream
                data = documentReader.read(Files.newInputStream(tmpFile), mediaType, metadataType);
                MetadataInfo metadataInfo = createMetadataInfoWithDefaultPermissions(data, user, mediaType, catalogue); //get the metadata info
                data.setMetadata(metadataInfo);

                id = Optional.ofNullable(documentIdentifierService.generateFileId(data.getId()))
                    .orElse(documentIdentifierService.generateFileId());

                // Check before the raw blob is committed, not just in the private save() below:
                // that commit lands first, so rejecting afterwards would leave an orphaned raw
                // upload in the datastore with no document to go with it.
                validateNoMojibake(data, id);
                validateRelationshipTargets(user, data, id);

                repo.save(user, id, message, metadataInfo, (o) -> Files.copy(tmpFile, o));
            } finally {
                Files.delete(tmpFile); //file no longer needed
            }

            return save(user, data, id, String.format("File upload for id: %s", id));

        } catch (IOException | UnknownContentTypeException ex) {
            throw new DocumentRepositoryException(
                String.format("File upload save failed for user: %s", user.getUsername()),
                ex
            );
        }
    }

    @Override
    public MetadataDocument saveNew(
        CatalogueUser user,
        MetadataDocument document,
        String catalogue,
        String message
    ) throws DocumentRepositoryException {
        try {
            return save(
                user,
                document,
                createMetadataInfoWithDefaultPermissions(document, user, MediaType.APPLICATION_JSON, catalogue),
                documentIdentifierService.generateFileId(),
                message,
                null
            );
        } catch (DataRepositoryException ex) {
            throw new DocumentRepositoryException(
                String.format("Saving new file: %s failed for user: %s", document.getId(), user.getUsername()),
                ex
            );
        }
    }

    @Override
    public MetadataDocument save(
        CatalogueUser user,
        MetadataDocument document,
        String id,
        String message
    ) throws DocumentRepositoryException {
        return save(user, document, id, message, null);
    }

    @Override
    public MetadataDocument save(
        CatalogueUser user,
        MetadataDocument document,
        String id,
        String message,
        String expectedRevision
    ) throws DocumentRepositoryException {
        try {
            return save(user,
                document,
                retrieveMetadataInfoUpdatingRawType(document),
                id,
                message,
                expectedRevision
            );
        } catch (DocumentRepositoryException | IOException | PostProcessingException | UnknownContentTypeException ex) {
            throw new DocumentRepositoryException(
                String.format(
                    "Saving file: %s failed for user: %s",
                    id,
                    user.getUsername()
                ),
                ex
            );
        }
    }

    @Override
    public MetadataDocument save(
        CatalogueUser user,
        MetadataDocument document,
        String message
    ) throws DocumentRepositoryException {
        try {
            return save(user,
                document,
                retrieveMetadataInfoUpdatingRawType(document),
                document.getId(),
                message,
                null
            );
        } catch (DocumentRepositoryException | IOException | PostProcessingException | UnknownContentTypeException ex) {
            throw new DocumentRepositoryException(
                String.format(
                    "Saving file: %s failed for user: %s",
                    document.getId(),
                    user.getUsername()
                ),
                ex
            );
        }
    }

    private MetadataDocument save(
        CatalogueUser user,
        MetadataDocument document,
        MetadataInfo metadataInfo,
        String id,
        String message,
        String expectedRevision
    ) throws DataRepositoryException, DocumentRepositoryException {
        updateIdAndMetadataDate(document, id);
        String uri = documentIdentifierService.generateUri(id);
        addRecordUriAsResourceIdentifier(document, uri);
        document.setUri(uri);
        validateUniqueResourceIdentifiers(document, id);
        validateNoMojibake(document, id);
        validateRelationshipTargets(user, document, id);
        repo.save(
            user,
            id,
            message,
            metadataInfo,
            (o) -> documentWriter.write(document, MediaType.APPLICATION_JSON, o),
            expectedRevision,
            document
        );

        return document;
    }

    @Override
    public DataRevision<CatalogueUser> delete(CatalogueUser user, String id) throws DocumentRepositoryException {
        try {
            return repo.delete(user, id);
        } catch (DataRepositoryException ex) {
            throw new DocumentRepositoryException(
                String.format(
                    "Cannot delete file: %s for user: %s",
                    id,
                    user.getUsername()
                ),
                ex
            );
        }
    }

    @Override
    public DataRevision<CatalogueUser> delete(CatalogueUser user, String id, String message) throws DocumentRepositoryException {
        try {
            return repo.delete(user, id, message);
        } catch (DataRepositoryException ex) {
            throw new DocumentRepositoryException(
                String.format(
                    "Cannot delete file: %s for user: %s",
                    id,
                    user.getUsername()
                ),
                ex
            );
        }
    }

    private MetadataInfo createMetadataInfoWithDefaultPermissions(MetadataDocument document, CatalogueUser user, MediaType mediaType, String catalogue) {
        MetadataInfo toReturn = MetadataInfo.builder()
            .rawType(mediaType.toString())
            .documentType(documentTypeLookupService.getName(document.getClass()))
            .catalogue(catalogue)
            .build();
        String username = user.getUsername();
        toReturn.addPermission(Permission.VIEW, username);
        toReturn.addPermission(Permission.EDIT, username);
        toReturn.addPermission(Permission.DELETE, username);
        return toReturn;
    }

    private void updateIdAndMetadataDate(MetadataDocument document, String id) {
        document.setId(id).setMetadataDate(LocalDateTime.now());
    }

    private void validateUniqueResourceIdentifiers(MetadataDocument document, String currentId) {

        if (document.getResourceIdentifiers() == null) return;

        for (ResourceIdentifier ri : document.getResourceIdentifiers()) {
            String code = ri.getCode();
            String codeSpace = ri.getCodeSpace();

            if (code == null || codeSpace == null || code.isBlank() || codeSpace.isBlank()) continue;

            String combined = codeSpace + ":" + code;

            resourceIdentifierLookupService.findDocumentIdsByRi(combined).stream()
                .filter(ownerId -> !ownerId.equals(currentId))
                .findFirst()
                .ifPresent(ownerId -> {
                    throw new ResourceIdentifierExistsException(
                        "A document with Resource Identifier \"" + combined +
                            "\" already exists (id = " + ownerId + "). " +
                            "Resource identifiers must be unique."
                    );
                });
        }
    }

    /**
     * Guards against dri-one #328 (double-encoded/"mojibake" literals) by scanning the document as
     * it will actually be written for the CP1252-decoded-as-UTF-8 signature.
     *
     * <p>Rejects only what this save <em>introduces</em>, comparing against what the stored version
     * already contains. The corruption predates the guard - #328's own verification query exists
     * because production records already match it - so failing any save that merely *contains* a
     * match would make every one of those records uneditable, and an editor fixing an unrelated
     * typo would get a 400 they cannot act on. Counting occurrences rather than comparing a set
     * means an existing {@code Â©} is tolerated while a second one pasted elsewhere is still
     * caught.
     *
     * <p>A document with no stored version (a create) is compared against nothing, so any match in
     * it is new and is rejected.
     */
    private void validateNoMojibake(MetadataDocument document, String id) {
        Map<String, Long> incoming = mojibakeCounts(objectMapper.writeValueAsString(document));
        if (incoming.isEmpty()) {
            return;
        }
        Map<String, Long> existing = storedMojibakeCounts(id);
        String introduced = incoming.entrySet().stream()
            .filter(e -> e.getValue() > existing.getOrDefault(e.getKey(), 0L))
            .map(Map.Entry::getKey)
            .sorted()
            .collect(Collectors.joining(", "));
        if (!introduced.isEmpty()) {
            // Deliberately self-contained and free of issue references or internal jargon: whoever
            // sees this is a depositor trying to save a record, not someone who can look up a
            // ticket. Name the characters found and say what to do about them.
            throw new MojibakeTextException(
                "Document " + id + " contains new text with misread characters: " + introduced +
                    ". Sequences like these appear where a quotation mark, apostrophe, dash or " +
                    "symbol was intended, and usually come from text copied out of a PDF or web " +
                    "page. Please retype the affected text, or paste it as plain text."
            );
        }
    }

    /**
     * Occurrences of the mojibake signature already present in the stored version of a document.
     * An unreadable or absent document counts as none, so a create - or a read failure - leaves
     * the guard at its strictest rather than silently allowing corruption through.
     */
    private Map<String, Long> storedMojibakeCounts(String id) {
        try {
            MetadataDocument stored = documentBundleReader.readBundle(id);
            return stored == null ? Map.of() : mojibakeCounts(objectMapper.writeValueAsString(stored));
        } catch (IOException | PostProcessingException
                 | UnknownContentTypeException | IllegalArgumentException ex) {
            // DataRepositoryException is an IOException subclass, so it is covered above.
            log.debug("No readable stored version of {} to compare mojibake against", id, ex);
            return Map.of();
        }
    }

    private static Map<String, Long> mojibakeCounts(String text) {
        return MOJIBAKE_PATTERN.matcher(text).results()
            .map(MatchResult::group)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    /**
     * Rejects a save that adds a relationship to a kind of record that relationship cannot point
     * at, e.g. a "produced at" ({@code doo:utilises}) link to anything but a monitoring facility
     * or network. {@link RelationshipRules} holds the rules, and the editor's record picker only
     * offers what they allow, but the API and hand-edited JSON bypass the picker (dri-one #404,
     * #439). A relationship with no rule is open and never checked.
     *
     * <p>Like {@link #validateNoMojibake}, only relationships this save <em>introduces</em> are
     * checked. One already stored is left alone, so a record holding one stays editable; for
     * {@code doo:utilises} its page and JSON drop it anyway ({@link Utilises#targets}). Checking
     * only new relationships also keeps an ordinary save from reading every target it already
     * points at.
     */
    private void validateRelationshipTargets(CatalogueUser user, MetadataDocument document, String id) {
        Set<Relationship> incoming = restrictedRelationships(document);
        if (incoming.isEmpty()) {
            return;
        }
        Set<Relationship> existing = storedRestrictedRelationships(id);
        String sourceType = document.getType();
        String invalid = incoming.stream()
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
     * Restricted relationships already in the stored version of a document. As with
     * {@link #storedMojibakeCounts}, an absent or unreadable document counts as none, so every
     * relationship in a create - or after a read failure - is checked. A read failure other than
     * absence is logged, as it means relationships the user did not touch are checked too.
     */
    private Set<Relationship> storedRestrictedRelationships(String id) {
        try {
            MetadataDocument stored = documentBundleReader.readBundle(id);
            return stored == null ? Set.of() : restrictedRelationships(stored);
        } catch (IOException | PostProcessingException
                 | UnknownContentTypeException | IllegalArgumentException ex) {
            if (isNotFound(ex)) {
                log.debug("No stored version of {} to compare relationships against", id);
            } else {
                log.warn("Cannot read the stored version of {}, so all its relationships are checked", id, ex);
            }
            return Set.of();
        }
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
            if (isNotFound(ex)) {
                log.debug("Relationship target {} does not exist", targetId);
                return false;
            }
            throw cannotCheck(targetId, ex);
        } catch (RuntimeException ex) {
            throw cannotCheck(targetId, ex);
        }
    }

    /**
     * The type of the record a target id names, or null if there is no such record.
     * <p>
     * A link document stands in for a record from another catalogue (UK-SCAPE holds dozens), and
     * search indexes it with that record's fields, so the picker offers it as that kind of record.
     * Its own type is empty, so the type is the linked record's. The user's view permission is
     * checked on the link document, the record they chose.
     */
    private String recordType(String targetId) {
        MetadataDocument target = readTarget(targetId);
        if (target instanceof LinkDocument link) {
            target = Optional.ofNullable(documentId(link.getLinkedDocumentId()))
                .map(this::readTarget)
                .orElse(null);
        }
        return target == null ? null : target.getType();
    }

    private MetadataDocument readTarget(String id) {
        try {
            return documentBundleReader.readBundle(id);
        } catch (Exception ex) {
            if (isNotFound(ex)) {
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

    /** Whether an exception, or anything that caused it, means the record does not exist. */
    private static boolean isNotFound(Throwable ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof GitFileNotFoundException) {
                return true;
            }
        }
        return false;
    }

    private void addRecordUriAsResourceIdentifier(MetadataDocument document, String recordUri) {
        List<ResourceIdentifier> resourceIdentifiers;

        if (document.getResourceIdentifiers() != null) {
            resourceIdentifiers = new ArrayList<>(document.getResourceIdentifiers());
        } else {
            resourceIdentifiers = new ArrayList<>();
        }

        ResourceIdentifier self = ResourceIdentifier.builder()
            .code(recordUri)
            .build();

        if (!resourceIdentifiers.contains(self)) {
            resourceIdentifiers.add(self);
        }
        document.setResourceIdentifiers(resourceIdentifiers);
    }

    private MetadataInfo retrieveMetadataInfoUpdatingRawType(MetadataDocument document)
        throws IOException, UnknownContentTypeException, PostProcessingException {
        return document.getMetadata().withRawType(MediaType.APPLICATION_JSON_VALUE);
    }
}
