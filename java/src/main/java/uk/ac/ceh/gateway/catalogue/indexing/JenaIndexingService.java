package uk.ac.ceh.gateway.catalogue.indexing;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.update.UpdateExecutionFactory;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

import java.util.List;

/**
 * This is the Jena Indexing Service. Instances of this can read documents from
 * a DataRepository and index them with the supplied IndexGenerator. The indexes
 * will then go into a Jena Triple Store for later retrieval.
 * @param <D> type of documents to be read from the DataRepository
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
        return perform(ReadWrite.READ, () -> jenaTdb.getDefaultModel().isEmpty());
    }
    
    @Override
    public void rebuildIndex() throws DocumentIndexingException {
        perform(ReadWrite.WRITE, () -> {
            super.rebuildIndex();
            return null;
        });
    }
    
    @Override
    public void indexDocuments(List<String> documents, String revision) throws DocumentIndexingException {
        perform(ReadWrite.WRITE, () -> {
            unindexDocuments(documents);
            super.indexDocuments(documents, revision); // index documents already calls commit();
            return null;
        });
    }
        
    @Override
    public void unindexDocuments(List<String> documents) throws DocumentIndexingException {
        perform(ReadWrite.WRITE, () -> {
            documents.stream().map((document) -> {
                //Remove any triples where this document uri is the subject
                ParameterizedSparqlString pss = new ParameterizedSparqlString("DELETE WHERE { ?id ?p ?o }");
                pss.setParam(
                    "id",
                    ResourceFactory.createResource(
                        documentIdentifierService.generateUri(document)
                    )
                );
                return pss;
            }).forEach((pss) -> {
                UpdateExecutionFactory.create(pss.asUpdate(), jenaTdb).execute();
            });
            return null;
        });
    }
    
    @Override
    protected void clearIndex() throws DocumentIndexingException {
        jenaTdb.getDefaultModel().removeAll();
    }

    @Override
    protected void index(List<Statement> toIndex) throws Exception {
        jenaTdb.getDefaultModel().add(toIndex);
    }
    
    // Define the transaction around advice for performing a jena transaction.
    // This method will not recreate a transaction if one is already on going.
    // @see https://jena.apache.org/documentation/tdb/tdb_transactions.html
    private <T> T perform(ReadWrite action, DocumentIndexingSupplier<T> supplier) throws DocumentIndexingException {
        if(jenaTdb.isInTransaction()) {
            return supplier.get();
        }
        else {
            jenaTdb.begin(action);
            try {
                return supplier.get();
            }
            finally {
                if(ReadWrite.WRITE.equals(action)) {
                    jenaTdb.commit(); // If in a write transaction then commit
                }
                jenaTdb.end();
            }
        }
    }
}
