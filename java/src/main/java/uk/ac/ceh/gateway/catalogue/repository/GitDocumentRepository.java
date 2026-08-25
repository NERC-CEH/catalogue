package uk.ac.ceh.gateway.catalogue.repository;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.document.UnknownContentTypeException;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.document.writing.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
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
import java.util.regex.Pattern;

@Slf4j
@ToString(onlyExplicitlyIncluded = true)
public class GitDocumentRepository implements DocumentRepository {
    /**
     * Telltale signature of double-encoded ("mojibake") text - UTF-8 bytes decoded as
     * CP1252/Latin-1 and re-encoded as UTF-8 - matching the verification query from dri-one #328
     * ({@code REGEX(STR(?o), "â€|Â[^ ]")}). Guards against any ingest path (known or not yet
     * found) baking further corrupted literals into the store.
     */
    private static final Pattern MOJIBAKE_PATTERN = Pattern.compile("â€|Â[^ ]");

    private final DocumentTypeLookupService documentTypeLookupService;
    private final DocumentReadingService documentReader;
    private final DocumentIdentifierService documentIdentifierService;
    private final DocumentWritingService documentWriter;
    private final BundledReaderService<MetadataDocument> documentBundleReader;
    private final ResourceIdentifierLookupService resourceIdentifierLookupService;
    private final GitRepoWrapper repo;
    private final JsonMapper objectMapper;

    public GitDocumentRepository(
        DocumentTypeLookupService documentTypeLookupService,
        DocumentReadingService documentReader,
        DocumentIdentifierService documentIdentifierService,
        DocumentWritingService documentWriter,
        BundledReaderService<MetadataDocument> documentBundleReader,
        ResourceIdentifierLookupService resourceIdentifierLookupService,
        GitRepoWrapper repo,
        JsonMapper objectMapper
    ) {
        this.documentTypeLookupService = documentTypeLookupService;
        this.documentReader = documentReader;
        this.documentIdentifierService = documentIdentifierService;
        this.documentWriter = documentWriter;
        this.documentBundleReader = documentBundleReader;
        this.resourceIdentifierLookupService = resourceIdentifierLookupService;
        this.repo = repo;
        this.objectMapper = objectMapper;
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
     * Guards against dri-one #328 (double-encoded/"mojibake" literals): scans the document as it
     * will actually be written for the CP1252-decoded-as-UTF-8 signature and refuses the save if
     * found, rather than baking further corrupted literals into the store.
     */
    private void validateNoMojibake(MetadataDocument document, String id) {
        String json = objectMapper.writeValueAsString(document);
        if (MOJIBAKE_PATTERN.matcher(json).find()) {
            throw new MojibakeTextException(
                "Document " + id + " contains text matching the double-encoding (mojibake) signature " +
                    "described in dri-one #328 (e.g. â€ or Â immediately followed by a " +
                    "non-space character). Please re-enter the affected text using its intended characters."
            );
        }
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
