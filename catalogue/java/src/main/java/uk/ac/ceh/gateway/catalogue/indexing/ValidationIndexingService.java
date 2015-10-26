package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.validation.ValidationReport;


/**
 * The following ValidationIndexingService checks a document against some 
 * validation checks.
 * @author cjohn
 */
public class ValidationIndexingService<D> extends AbstractIndexingService<D, ValidationReport> {
    private final Map<String, ValidationReport> results;

    public ValidationIndexingService(
            BundledReaderService<D> reader, 
            DocumentListingService listingService, 
            DataRepository<?> repo, 
            IndexGenerator<D, ValidationReport> indexGenerator) {
        super(reader, listingService, repo, indexGenerator);
        results = new HashMap<>();
    }
    
    @Override
    public boolean isIndexEmpty() throws DocumentIndexingException {
        return results.isEmpty();
    }

    @Override
    public void clearIndex() throws DocumentIndexingException {
        results.clear();
    }

    @Override
    public void index(ValidationReport toIndex) throws Exception {
        results.put(toIndex.getDocumentId(), toIndex);
    }

    @Override
    public void unindexDocuments(List<String> unIndex) throws DocumentIndexingException {
        unIndex.stream().forEach(results::remove);
    }

    @Override
    public void commit() throws DocumentIndexingException {}
    
    public List<ValidationReport> getResults() {
        return new ArrayList<>(results.values());
    }
}
