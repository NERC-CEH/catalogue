package uk.ac.ceh.gateway.catalogue.indexing.jena;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.update.UpdateExecutionFactory;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.indexing.AbstractIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingSupplier;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreement;

import java.util.List;

/**
 * This is the Jena Indexing Service. Instances of this can read documents from
 * a DataRepository and index them with the supplied IndexGenerator. The indexes
 * will then go into a Jena Triple Store for later retrieval.
 * @param <D> type of documents to be read from the DataRepository
 */
@Slf4j
@ToString(callSuper = true)
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
        log.info("Creating");
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
    protected boolean canIndex(D doc) {
        if (log.isDebugEnabled() && doc instanceof MetadataDocument document) {
            log.debug("can index? {}", document.getId());
        }
        if (doc == null) {
            return false;
        }
        return !(doc instanceof ServiceAgreement);
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
            documents.stream()
                .filter(document -> {
                    try {
                        return canIndex(readDocument(document));
                    } catch (Exception e) {
                       return false;
                    }
                })
                .map(document -> {
                    //Remove any triples where this document uri is the subject
                    ParameterizedSparqlString pss = new ParameterizedSparqlString("DELETE WHERE { ?id ?p ?o }");
                    pss.setParam(
                        "id",
                        ResourceFactory.createResource(
                            documentIdentifierService.generateUri(document)
                        )
                    );
                    return pss;
                })
                .forEach((pss) -> {
                    UpdateExecutionFactory.create(pss.asUpdate(), jenaTdb).execute();
                });
            return null;
        });
    }

    @Override
    protected void clearIndex() {
        jenaTdb.getDefaultModel().removeAll();
    }

    @Override
    protected void index(List<Statement> toIndex) {
        jenaTdb.getDefaultModel().add(toIndex);
    }

    // Define the transaction around advice for performing a jena transaction.
    // This method will not recreate a transaction if one is already on going.
    // @see https://jena.apache.org/documentation/tdb/tdb_transactions.html
    @SneakyThrows
    private <T> T perform(ReadWrite action, DocumentIndexingSupplier<T> supplier) {
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
