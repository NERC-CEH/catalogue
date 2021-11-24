package uk.ac.ceh.gateway.catalogue.indexing.validation;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.indexing.AbstractIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.validation.ValidationReport;

import java.util.*;


/**
 * The following ValidationIndexingService checks a document against some
 * validation checks.
 * @param <D>
 */
@Slf4j
@ToString(callSuper = true, exclude = {"failed", "results"})
public class ValidationIndexingService<D extends MetadataDocument> extends AbstractIndexingService<D, ValidationReport> {
    private final Map<String, ValidationReport> results;
    private final Set<String> failed;
    private final PostProcessingService<D> postProcessingService;
    private final DocumentIdentifierService documentIdentifierService;

    public ValidationIndexingService(
            BundledReaderService<D> reader,
            DocumentListingService listingService,
            DataRepository<?> repo,
            PostProcessingService<D> postProcessingService,
            DocumentIdentifierService documentIdentifierService,
            IndexGenerator<D, ValidationReport> indexGenerator
    ) {
        super(reader, listingService, repo, indexGenerator);
        this.results = new HashMap<>();
        this.failed = new HashSet<>();
        this.postProcessingService = postProcessingService;
        this.documentIdentifierService = documentIdentifierService;
        log.info("Creating {}", this);
    }

    @Override
    public boolean isIndexEmpty() {
        return results.isEmpty() && failed.isEmpty();
    }

    @Async
    @Override
    protected void clearIndex() {
        results.clear();
        failed.clear();
    }

    @Async
    @Override
    public void indexDocuments(List<String> documents, String revision) throws DocumentIndexingException {
        try {
            super.indexDocuments(documents, revision);
        }
        catch(DocumentIndexingException die) {
            failed.addAll(die.getSupressedDocuments());
            throw die;
        }
    }

    @Override
    protected void index(ValidationReport toIndex) {
        results.put(toIndex.getDocumentId(), toIndex);
    }

    @Async
    @Override
    public void unindexDocuments(List<String> unIndex) {
        unIndex.forEach(results::remove);
    }

    /**
     * @return a list of validation reports for each of the documents which have
     * been validated
     */
    public List<ValidationReport> getResults() {
        return new ArrayList<>(results.values());
    }

    /**
     * @return the list of documents which completely failed to index. That is
     * they could not even be opened for validation
     */
    public List<String> getFailed() {
        return new ArrayList<>(failed);
    }

    @Override
    @SneakyThrows
    protected D readDocument(String document, String revision) {
        D toReturn = super.readDocument(document, revision);
        toReturn.setUri(documentIdentifierService.generateUri(document));
        postProcessingService.postProcess(toReturn);
        return toReturn;
    }
}
