package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.validation.ValidationReport;


/**
 * The following ValidationIndexingService checks a document against some 
 * validation checks.
 * @author cjohn
 * @param <D>
 */
public class ValidationIndexingService<D extends MetadataDocument> extends AbstractIndexingService<D, ValidationReport> {
    private final Map<String, ValidationReport> results;
    private final Set<String> failed;
    private final PostProcessingService postProcessingService;
    private final DocumentIdentifierService documentIdentifierService;

    public ValidationIndexingService(
            BundledReaderService<D> reader, 
            DocumentListingService listingService, 
            DataRepository<?> repo,
            PostProcessingService<D> postProcessingService,
            DocumentIdentifierService documentIdentifierService,
            IndexGenerator<D, ValidationReport> indexGenerator) {
        super(reader, listingService, repo, indexGenerator);
        results = new HashMap<>();
        failed = new HashSet<>();
        
        this.postProcessingService = postProcessingService;
        this.documentIdentifierService = documentIdentifierService;
    }
    
    @Override
    public boolean isIndexEmpty() throws DocumentIndexingException {
        return results.isEmpty() && failed.isEmpty();
    }

    @Override
    protected void clearIndex() throws DocumentIndexingException {
        results.clear();
        failed.clear();
    }
    
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
    protected void index(ValidationReport toIndex) throws Exception {
        results.put(toIndex.getDocumentId(), toIndex);
    }

    @Override
    public void unindexDocuments(List<String> unIndex) throws DocumentIndexingException {
        unIndex.stream().forEach(results::remove);
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
    protected D readDocument(String document, String revision) throws Exception {
        D toReturn = super.readDocument(document, revision);
        toReturn.setUri(documentIdentifierService.generateUri(document));
        postProcessingService.postProcess(toReturn);
        return toReturn;
    }
}
