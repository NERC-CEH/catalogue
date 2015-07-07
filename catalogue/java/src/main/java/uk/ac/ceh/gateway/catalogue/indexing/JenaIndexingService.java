package uk.ac.ceh.gateway.catalogue.indexing;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import java.util.List;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

/**
 *
 * @author cjohn
 * @param <D>
 */
public class JenaIndexingService<D> extends AbstractIndexingService<D, List<Statement>> {
    private final DocumentIdentifierService documentIdentifierService;
    private final Dataset jenaTdb;

    public JenaIndexingService(
            BundledReaderService<D> reader,
            DocumentListingService listingService,
            DataRepository<?> repo,
            IndexGenerator<D, List<Statement>> indexGenerator,
            DocumentIdentifierService documentIdentifierService,
            Dataset jenaTdb) {
        super(reader, listingService, repo, indexGenerator);
        this.documentIdentifierService = documentIdentifierService;
        this.jenaTdb = jenaTdb;
    }
    
    @Override
    public boolean isIndexEmpty() throws DocumentIndexingException {
       return jenaTdb.getDefaultModel().isEmpty();
    }
        
    @Override
    public void unindexDocuments(List<String> documents) throws DocumentIndexingException {
        GraphStore graph = GraphStoreFactory.create(jenaTdb);
        for(String document : documents) {
            //Remove any triples where this given document id is the subject or object
            ParameterizedSparqlString pss = new ParameterizedSparqlString("DELETE WHERE { ?id ?p ?o }; DELETE WHERE { ?s ?p ?id }");
            pss.setParam("id", ResourceFactory.createResource(documentIdentifierService.generateUri(document)));
            UpdateExecutionFactory.create(pss.asUpdate(), graph).execute();
        }
    }
    
    @Override
    public void clearIndex() throws DocumentIndexingException {
        jenaTdb.getDefaultModel().removeAll();
    }

    @Override
    public void index(List<Statement> toIndex) throws Exception {
        jenaTdb.getDefaultModel().add(toIndex);
    }

    @Override
    public void commit() throws DocumentIndexingException {}
}
