package uk.ac.ceh.gateway.catalogue.datapreviewer;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.git.GitFileNotFoundException;
import uk.ac.ceh.gateway.catalogue.datapreviewer.PreviewManifest.PreviewFile;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

/**
 * What a record's data preview shows: its file listing, a CSV's first rows and a supporting
 * document's content.
 *
 * <p>Every call checks the record is open before any cache is consulted, so a record closed since
 * its listing was cached shows nothing. The listing and the content are cached by
 * {@link PreviewListing} and {@link PreviewContentReader}, separate beans so each call crosses a
 * caching proxy. Only paths in the record's listing are ever read.
 */
@Service
public class DataPreviewService {

    private final DocumentRepository documentRepository;
    private final OpenDataAccess openDataAccess;
    private final PreviewListing listing;
    private final PreviewContentReader contentReader;

    public DataPreviewService(
        DocumentRepository documentRepository,
        OpenDataAccess openDataAccess,
        PreviewListing listing,
        PreviewContentReader contentReader
    ) {
        this.documentRepository = documentRepository;
        this.openDataAccess = openDataAccess;
        this.listing = listing;
        this.contentReader = contentReader;
    }

    /**
     * @throws NoSuchElementException  when the record does not exist
     * @throws PreviewNotOpenException when the record's data is not openly accessible
     */
    public PreviewManifest manifest(String recordId) {
        requireOpen(recordId);
        return listing.manifest(recordId);
    }

    /**
     * @return the CSV's first rows, or empty when the path is not one of the record's listed CSVs
     * @throws NoSuchElementException  when the record does not exist
     * @throws PreviewNotOpenException when the record's data is not openly accessible
     */
    public Optional<CsvSample> csv(String recordId, String path) {
        return find(manifest(recordId).csvFiles(), path)
            .map(file -> contentReader.csv(recordId, file.path(), file.sha256()));
    }

    /**
     * @return the document's content, or empty when the path is not one of the record's listed
     *         supporting documents
     * @throws NoSuchElementException  when the record does not exist
     * @throws PreviewNotOpenException when the record's data is not openly accessible
     * @throws PreviewBusyException    when every conversion worker is busy
     */
    public Optional<DocumentPreview> document(String recordId, String path) {
        return find(manifest(recordId).documents(), path)
            .map(file -> contentReader.document(recordId, file.path(), file.sha256()));
    }

    private void requireOpen(String recordId) {
        MetadataDocument document;
        try {
            document = documentRepository.read(recordId);
        } catch (DocumentRepositoryException e) {
            if (isNotFound(e)) {
                throw new NoSuchElementException("No record " + recordId);
            }
            throw new IllegalStateException("Cannot read record " + recordId, e);
        }
        if (document == null) {
            throw new NoSuchElementException("No record " + recordId);
        }
        if (!(document instanceof GeminiDocument gemini) || !openDataAccess.isOpen(gemini)) {
            throw new PreviewNotOpenException(recordId);
        }
    }

    private static boolean isNotFound(Throwable e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof GitFileNotFoundException) {
                return true;
            }
        }
        return false;
    }

    private static Optional<PreviewFile> find(List<PreviewFile> files, String path) {
        return files.stream().filter(file -> file.path().equals(path)).findFirst();
    }
}
