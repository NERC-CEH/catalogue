package uk.ac.ceh.gateway.catalogue.repository.checks;

import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.components.datastore.git.GitFileNotFoundException;
import uk.ac.ceh.gateway.catalogue.document.UnknownContentTypeException;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;

import java.io.IOException;
import java.util.Optional;

/**
 * The version of a document already in the datastore, which checks compare a save against so they
 * refuse only what the save <em>introduces</em>.
 * <p>
 * It is read lazily, at most once per save, however many checks ask: most saves need no check to
 * look at it at all.
 * <p>
 * An absent or unreadable document counts as none, so a create - or a read failure - leaves every
 * check at its strictest rather than silently letting something through. A read failure other than
 * absence is logged, as it means the checks see the whole document as new.
 */
@Slf4j
public final class StoredVersion {
    private final BundledReaderService<MetadataDocument> reader;
    private final String id;
    private Optional<MetadataDocument> stored;

    private StoredVersion(BundledReaderService<MetadataDocument> reader, String id) {
        this.reader = reader;
        this.id = id;
    }

    public static StoredVersion of(BundledReaderService<MetadataDocument> reader, String id) {
        return new StoredVersion(reader, id);
    }

    public Optional<MetadataDocument> get() {
        if (stored == null) {
            stored = read();
        }
        return stored;
    }

    private Optional<MetadataDocument> read() {
        try {
            return Optional.ofNullable(reader.readBundle(id));
        } catch (IOException | PostProcessingException
                 | UnknownContentTypeException | IllegalArgumentException ex) {
            // DataRepositoryException is an IOException subclass, so it is covered above.
            if (isNotFound(ex)) {
                log.debug("No stored version of {}", id);
            } else {
                log.warn("Cannot read the stored version of {}, so the save is checked as wholly new", id, ex);
            }
            return Optional.empty();
        }
    }

    /** Whether an exception, or anything that caused it, means the record does not exist. */
    static boolean isNotFound(Throwable ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof GitFileNotFoundException) {
                return true;
            }
        }
        return false;
    }
}
