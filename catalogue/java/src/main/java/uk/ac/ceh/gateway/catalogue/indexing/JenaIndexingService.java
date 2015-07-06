package uk.ac.ceh.gateway.catalogue.indexing;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import java.util.List;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

/**
 *
 * @author cjohn
 * @param <D>
 */
public class JenaIndexingService<D> extends AbstractIndexingService<D, List<Statement>> {
    private final Model model;

    public JenaIndexingService(
            BundledReaderService<D> reader,
            DocumentListingService listingService,
            DataRepository<?> repo,
            IndexGenerator<D, List<Statement>> indexGenerator,
            Model model) {
        super(reader, listingService, repo, indexGenerator);
        this.model = model;
    }
    
    @Override
    public boolean isIndexEmpty() throws DocumentIndexingException {
       return true;
    }
        
    @Override
    public void unindexDocuments(List<String> documents) throws DocumentIndexingException {
        
    }

    @Override
    public void clearIndex() throws DocumentIndexingException {
        
    }

    @Override
    public void index(List<Statement> toIndex) throws Exception {
        model.add(toIndex);
    }

    @Override
    public void commit() throws DocumentIndexingException {
    }
}
